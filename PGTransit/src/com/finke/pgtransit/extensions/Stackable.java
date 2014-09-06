package com.finke.pgtransit.extensions;

import android.os.Bundle;

/* Stackable objects are ones that can be pushed and popped,
 * as well as have their states persisted, by the StackController
 * class
 */
public interface Stackable {
	public void saveState(Bundle state);
	public void restoreState(Bundle state);
}
