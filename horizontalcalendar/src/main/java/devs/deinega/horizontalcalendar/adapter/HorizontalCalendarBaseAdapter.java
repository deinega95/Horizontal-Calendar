package devs.deinega.horizontalcalendar.adapter;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

import devs.deinega.horizontalcalendar.HorizontalCalendarView;
import devs.deinega.horizontalcalendar.model.CalendarItemStyle;
import devs.deinega.horizontalcalendar.utils.HorizontalCalendarListener;
import devs.deinega.horizontalcalendar.utils.HorizontalCalendarPredicate;
import devs.deinega.horizontalcalendar.HorizontalCalendar;
import devs.deinega.horizontalcalendar.HorizontalLayoutManager;
import devs.deinega.horizontalcalendar.R;
import devs.deinega.horizontalcalendar.utils.Utils;

/**
 * Base class for all adapters for {@link HorizontalCalendarView HorizontalCalendarView}
 *
 * @author Mulham-Raee
 * @since v1.3.0
 */
public abstract class HorizontalCalendarBaseAdapter<VH extends DateViewHolder, T extends Calendar> extends RecyclerView.Adapter<VH> {

    private final int itemResId;
    final HorizontalCalendar horizontalCalendar;
    private final HorizontalCalendarPredicate disablePredicate;
    private final int cellWidth;
    private CalendarItemStyle disabledItemStyle;

    protected Calendar startDate;
    protected int itemsCount;

    protected HorizontalCalendarBaseAdapter(int itemResId, final HorizontalCalendar horizontalCalendar, Calendar startDate, Calendar endDate, HorizontalCalendarPredicate disablePredicate) {
        this.itemResId = itemResId;
        this.horizontalCalendar = horizontalCalendar;
        this.disablePredicate = disablePredicate;
        this.startDate = startDate;
        if (disablePredicate != null) {
            this.disabledItemStyle = disablePredicate.style();
        }

        cellWidth = Utils.calculateCellWidth(horizontalCalendar.getContext(), horizontalCalendar.getNumberOfDatesOnScreen());
        itemsCount = calculateItemsCount(startDate, endDate);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(itemResId, parent, false);

        final VH viewHolder = createViewHolder(itemView, cellWidth);
        viewHolder.itemView.setOnClickListener(new MyOnClickListener(viewHolder));
        viewHolder.itemView.setOnLongClickListener(new MyOnLongClickListener(viewHolder));


        return viewHolder;
    }


    protected abstract VH createViewHolder(View itemView, int cellWidth);

    public abstract T getItem(int position);

    @Override
    public int getItemCount() {
        return itemsCount;
    }

    public boolean isDisabled(int position) {
        if (disablePredicate == null) {
            return false;
        }
        Calendar date = getItem(position);
        return disablePredicate.test(date);
    }


    protected void applyStyle(VH viewHolder, Calendar date, int position) {
        int selectedItemPosition = horizontalCalendar.getSelectedDatePosition();

        if (disablePredicate != null) {
            boolean isDisabled = disablePredicate.test(date);
            viewHolder.itemView.setEnabled(!isDisabled);
            if (isDisabled && (disabledItemStyle != null)) {
                applyStyle(viewHolder, disabledItemStyle);
                return;
            }
        }

        // Selected Day
        if (position == selectedItemPosition) {
            applyStyle(viewHolder, horizontalCalendar.getSelectedItemStyle());
            viewHolder.textMiddle.setBackgroundResource(R.drawable.background_date_selected);
        }
        // Unselected Days
        else {
            applyStyle(viewHolder, horizontalCalendar.getDefaultStyle());
            viewHolder.textMiddle.setBackgroundResource(0);
        }
    }

    protected void applyStyle(VH viewHolder, CalendarItemStyle itemStyle) {
        viewHolder.textTop.setTextColor(itemStyle.getColorTopText());
        viewHolder.textMiddle.setTextColor(itemStyle.getColorMiddleText());
        viewHolder.textBottom.setTextColor(itemStyle.getColorBottomText());

        if (Build.VERSION.SDK_INT >= 16) {
            viewHolder.itemView.setBackground(itemStyle.getBackground());
        } else {
            viewHolder.itemView.setBackgroundDrawable(itemStyle.getBackground());
        }
    }

    public void update(Calendar startDate, Calendar endDate, boolean notify) {
        this.startDate = startDate;
        itemsCount = calculateItemsCount(startDate, endDate);
        if (notify) {
            notifyDataSetChanged();
        }
    }

    protected abstract int calculateItemsCount(Calendar startDate, Calendar endDate);

    private class MyOnClickListener implements View.OnClickListener {
        private final RecyclerView.ViewHolder viewHolder;

        MyOnClickListener(RecyclerView.ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            int position = viewHolder.getAdapterPosition();
            if (position == -1)
                return;

            horizontalCalendar.getCalendarView().setSmoothScrollSpeed(HorizontalLayoutManager.SPEED_SLOW);
            horizontalCalendar.centerCalendarToPosition(position);
        }
    }

    private class MyOnLongClickListener implements View.OnLongClickListener {
        private final RecyclerView.ViewHolder viewHolder;

        MyOnLongClickListener(RecyclerView.ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public boolean onLongClick(View v) {
            HorizontalCalendarListener calendarListener = horizontalCalendar.getCalendarListener();
            if (calendarListener == null) {
                return false;
            }

            int position = viewHolder.getAdapterPosition();
            Calendar date = getItem(position);

            return calendarListener.onDateLongClicked(date, position);
        }
    }
}
