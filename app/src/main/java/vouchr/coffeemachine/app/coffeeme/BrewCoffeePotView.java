package vouchr.coffeemachine.app.coffeeme;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Bryan on 3/15/2017.
 */

public class BrewCoffeePotView extends LinearLayout {

    public BrewCoffeePotView(Context context) {
        super(context);
        init();
    }

    public BrewCoffeePotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BrewCoffeePotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.brew_new_coffee_pot, this);
    }
}
