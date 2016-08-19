package com.finke.pgtransit.extensions;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.finke.pgtransit.R;

/* This class manages the state of the backstack and visible fragments
 * for an activity. Fragments that want to be managed must implement
 * the Stackable interface. It handles pushing and popping the fragments,
 * including animations between pages or tabs. Furthermore, it forces the
 * Stackable objects to save and restore their view states.
 */
public class StackController {
	
	// Identifiers for state bundle contents
	private static final String STACK_NO_STR = "StackNo";
	private static final String STACK_COUNT_STR = "StackCount";
	private static final String STACK_SIZE_STR = "StackSize";
	private static final String VIEW_STATE = "ViewState";
	
	// Current stack no
	private int mStackNo;
	// Whether the controller is in operation, working with fragments
	private boolean mStopped;
	// Lists of lists of stackable fragments
	// First index = tab number
	// Second index = back stack index
	private ArrayList<ArrayList<Stackable>> mBackStacks;
	private FragmentManager mFm;
	// Temporary view state not yet written to activity state bundle
	private Bundle mViewState;
	
	public StackController(FragmentActivity activity) {
		mStackNo = 0;
		mStopped = false;
		mFm = activity.getSupportFragmentManager();
		mBackStacks = new ArrayList<ArrayList<Stackable>>();
		mViewState = new Bundle();
	}
	
	/* Iteratively has each fragment save their state into the bundle
	 * Also saves temp bundle into that state bundle
	 */
	public void saveState(Bundle state) {
		state.putInt(STACK_NO_STR, mStackNo);
		state.putInt(STACK_COUNT_STR, mBackStacks.size());
		for(int i = 0; i < mBackStacks.size(); i++) {
			ArrayList<Stackable> stack = mBackStacks.get(i);
			state.putInt(i + "|" + STACK_SIZE_STR, stack.size());
			// Each fragment does their work here
			for(int j = 0; j < stack.size(); j++) {
				Stackable frag = stack.get(j);
				// Fragments are restored by name
				// NO DUPLICATE PUSHES!
				// TODO: maybe fix this
				state.putString(i + "|" + j + "|name", frag.getClass().getCanonicalName());
				// Active fragments can save their own state
				if(i == mStackNo && j == stack.size()-1) {
					Bundle fragState = new Bundle();
					frag.saveState(fragState);
					state.putBundle(i + "|" + j + "|state", fragState);
				}
				// Backstack'd fragments have already saved their state in the temp bundle
				else {
					state.putBundle(i + "|" + j + "|state", mViewState.getBundle(i + "|" + j));
				}
			}
		}
		state.putBundle(VIEW_STATE, mViewState);
	}
	
	/* Record the state temporarily for a frag being backstack'd */
	public void saveStateInMemory(Stackable frag, String key) {
		Bundle state = new Bundle();
		frag.saveState(state);
		mViewState.putBundle(key, state);
	}
	
	/* Iteratively restore StackController state, as well as all of its
	 * child fragments. Backstack'd fragments have their states restored
	 * in the temp bundle
	 */
	public void restoreState(Bundle state) {
		mStackNo = state.getInt(STACK_NO_STR);
		int stackCount = state.getInt(STACK_COUNT_STR);
		for(int i = 0; i < stackCount; i++) {
			int stackSize = state.getInt(i + "|" + STACK_SIZE_STR);
			ArrayList<Stackable> newStack = new ArrayList<Stackable>();
			mBackStacks.add(newStack);
			// For each stackable fragment
			for(int j = 0; j < stackSize; j++) {
				try {
					// Instantiate that fragment using reflection
					Fragment frag = (Fragment)Class.forName(state.getString(i + "|" + j + "|name"))
						.getConstructor().newInstance();
					// Add it to backstack
					newStack.add((Stackable)frag);
					// Tell it to restore its state
					((Stackable)frag).restoreState(state.getBundle(i + "|" + j + "|state"));
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		// Restore in-memory state for fragments that were not visible
		mViewState = state.getBundle(VIEW_STATE);
		
		// Add the topmost fragment from the last used tab back in
		FragmentTransaction ft = mFm.beginTransaction();
		ArrayList<Stackable> lastStack = mBackStacks.get(mStackNo);
		ft.replace(R.id.fragmentContainer, (Fragment)lastStack.get(lastStack.size()-1));
		ft.commit();
	}
	
	/* Pull state from temp bundle when a fragment comes back in view */
	public void restoreStateInMemory(Stackable frag, String key) {
		frag.restoreState(mViewState.getBundle(key));
	}
	
	public int getStackNo() { return mStackNo; }
	public int getStackCount() { return mBackStacks.get(mStackNo).size(); }
	public int getStackCount(int stackNo) { return mBackStacks.get(stackNo).size(); }
	// Return the fragment that is visible
	public Fragment getActiveFragment() {
		ArrayList<Stackable> curStack = mBackStacks.get(mStackNo);
		return (Fragment)curStack.get(curStack.size()-1);
	}
	public Fragment getFragment(int stackNo, int index) {
		return (Fragment)mBackStacks.get(stackNo).get(index);
	}
	
	// Convenience method to push fragment on current tab's stack
	public void push(Stackable frag) {
		push(frag, mStackNo);
	}
	
	/* Push fragments onto a desired stack, saving state in the process */
	public void push(Stackable newFrag, int stackNo) {
		FragmentTransaction ft = mFm.beginTransaction();
		
		// No transition on first push of all stacks
		if(mBackStacks.size() != 0) {
			// Slide other way if tab switching from right to left
			if(stackNo < mStackNo) {
				ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
			}
			else {
				ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
			}
		}
		
		// Add a new stack if a new tab is being used
		if(mBackStacks.size() == stackNo) {
			mBackStacks.add(new ArrayList<Stackable>());
		}
		
		// Push new fragment on stack
		ArrayList<Stackable> stack = mBackStacks.get(mStackNo);
		if(!mBackStacks.get(stackNo).contains(newFrag)) {
			// Save previous fragment's view state
			if(!stack.isEmpty()) {
				saveStateInMemory(stack.get(stack.size()-1), mStackNo + "|" + (stack.size()-1));
			}
			ft.replace(R.id.fragmentContainer, (Fragment)newFrag, null);
			ft.commit();
			mBackStacks.get(stackNo).add(newFrag);
		}
		// Switch back to another tab
		else {
			saveStateInMemory(stack.get(stack.size()-1), mStackNo + "|" + (stack.size()-1));
			ArrayList<Stackable> selStack = mBackStacks.get(stackNo);
			ft.replace(R.id.fragmentContainer, (Fragment)selStack.get(selStack.size()-1), null);
			ft.commit();
			restoreStateInMemory(selStack.get(selStack.size()-1), stackNo + "|" + (selStack.size()-1));
		}

		mStackNo = stackNo;
	}
	
	/* Restore a state from the top of the back stack,
	 * pull its state from the temp state bundle
	 */
	public void pop() {
		FragmentTransaction ft = mFm.beginTransaction();
		ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
		ArrayList<Stackable> selStack = mBackStacks.get(mStackNo);
		selStack.remove(selStack.size()-1);
		ft.replace(R.id.fragmentContainer, (Fragment)selStack.get(selStack.size()-1), null);
		ft.commit();
		restoreStateInMemory(selStack.get(selStack.size()-1), mStackNo + "|" + (selStack.size()-1));
	}
	
	public boolean onBackPressed() {
		ArrayList<Stackable> stack = mBackStacks.get(mStackNo);
		Stackable frag = stack.get(stack.size()-1);
		return frag.onBackPressed();
	}
	
	/* Similar to a push, adds the top stack item to the view container
	 * but without animation
	 */
	public void start() {
		if(mStopped) {
			FragmentTransaction ft = mFm.beginTransaction();
			ArrayList<Stackable> lastStack = mBackStacks.get(mStackNo);
			Stackable frag = lastStack.get(lastStack.size()-1);
			frag.restoreState(mViewState.getBundle(mStackNo + "|" + (lastStack.size()-1)));
			ft.replace(R.id.fragmentContainer, (Fragment)frag);
			ft.commit();
			mStopped = false;
		}
	}
	
	/* Similar to a pop, removes the visible fragment and puts it on the
	 * back stack, but without animation
	 */
	public void stop() {
		if(!mStopped) {
			FragmentTransaction ft = mFm.beginTransaction();
			ArrayList<Stackable> curStack = mBackStacks.get(mStackNo);
			Bundle state = new Bundle();
			Stackable frag = curStack.get(curStack.size()-1);
			frag.saveState(state);
			mViewState.putBundle(mStackNo + "|" + (curStack.size()-1), state);
			ft.remove((Fragment)frag);
			ft.commit();
			mStopped = true;
		}
	}
}
