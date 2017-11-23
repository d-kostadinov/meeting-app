package kinect.pro.meetingapp.other;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

import kinect.pro.meetingapp.model.Contact;

public class ContactsAdapter extends ArrayAdapter {

    private ArrayList<Contact> items;
    private int viewResourceId;

    private ArrayList<Contact>filteredData = null;

    public ContactsAdapter(Context context, int viewResourceId, ArrayList<Contact> items) {
        super(context, viewResourceId, items);

        this.items = items;
        this.viewResourceId = viewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        ViewHolder holder;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = vi.inflate(viewResourceId, null);

            holder = new ViewHolder();
            holder.contact = v.findViewById(android.R.id.text1);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        try {
            Contact contact = filteredData.get(position);
            if (contact != null) {
                holder.contact.setText(String.format("%s - %s",
                        contact.getPhone(),
                        contact.getName()));
            }
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }

        return v;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return filteredData.get(position).getPhone();
    }

    @Override
    public int getCount() {
        return (filteredData == null)? items.size():filteredData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new ContactFilter();
    }

    private class ContactFilter extends Filter {

        private int lastConstraintSize = 0;

        @Override
        public String convertResultToString(Object resultValue) {
            return resultValue.toString();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                ArrayList<Contact> mSuggestions = new ArrayList<>();

                if (lastConstraintSize != 0 && lastConstraintSize < constraint.toString().length()){
                    for (Contact value : filteredData) {
                        if (value.getPhone().substring(0, constraint.toString().length())
                                .equals(constraint.toString())){
                            mSuggestions.add(value);
                        }
                    }
                } else {
                    for (Contact value : items) {
                        if (value.getPhone().substring(0, constraint.toString().length())
                                .equals(constraint.toString())){
                            mSuggestions.add(value);
                        }
                    }
                }

                lastConstraintSize = constraint.toString().length();

                FilterResults filterResults = new FilterResults();
                filterResults.values = mSuggestions;
                filterResults.count = mSuggestions.size();

                return filterResults;
            } else {
                return null;
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                filteredData = (ArrayList<Contact>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    static class ViewHolder {
        TextView contact;
    }
}
