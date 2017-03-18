package vouchr.coffeemachine.app.coffeeme;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import vouchr.coffee.models.CoffeePot;

/**
 * Created by Bryan on 3/17/2017.
 */

public class ListViewCellCoffeePot extends LinearLayout {

    @BindView(R.id.dateTextView)
    TextView dateTextView;
    @BindView(R.id.tablespoonsTextView)
    TextView tablespoonsTextView;
    @BindView(R.id.beanImageView)
    ImageView beanImageView;
    @BindView(R.id.beanTypeTextView)
    TextView beanTypeTextView;
    @BindView(R.id.roastTypeImageView)
    ImageView roastTypeImageView;

    public ListViewCellCoffeePot(Context context) {
        super(context);
        init();
    }

    public ListViewCellCoffeePot(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListViewCellCoffeePot(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.listview_cell_coffee_pot, this);
        ButterKnife.bind(this);
    }

    public void setCoffeePot(CoffeePot coffeePot) {
        dateTextView.setText(coffeePot.getDateString());
        tablespoonsTextView.setText(String.format(getContext().getString(R.string.format_tbsp), String.valueOf(coffeePot.getTbspCount())));
        beanTypeTextView.setText(coffeePot.getBeanName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (coffeePot.getRoast().equals("Dark")) {
                roastTypeImageView.setColorFilter(getContext().getColor(R.color.darkRoast));
            } else if (coffeePot.getRoast().equals("Light")) {
                roastTypeImageView.setColorFilter(getContext().getColor(R.color.lightRoast));
            } else if (coffeePot.getRoast().equals("Medium")) {
                roastTypeImageView.setColorFilter(getContext().getColor(R.color.mediumRoast));
            }
        } else {
            if (coffeePot.getRoast().equals("Dark")) {
                roastTypeImageView.setColorFilter(getContext().getResources().getColor(R.color.darkRoast));
            } else if (coffeePot.getRoast().equals("Light")) {
                roastTypeImageView.setColorFilter(getContext().getResources().getColor(R.color.lightRoast));
            } else if (coffeePot.getRoast().equals("Medium")) {
                roastTypeImageView.setColorFilter(getContext().getResources().getColor(R.color.mediumRoast));
            }
        }
//        beanImageView.setImageResource(R.drawable.circle_beans);
    }
}
