/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2022 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.ui.recyclerview.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.catrobat.catroid.R;
import org.catrobat.catroid.ui.recyclerview.viewholder.ViewHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryListRVAdapter extends RecyclerView.Adapter<ViewHolder> {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DEFAULT, COLLISION, NXT, EV3})
    public @interface CategoryListItemType{}
    public static final int DEFAULT = 0;
    public static final int COLLISION = 1;
    public static final int NXT = 2;
    public static final int EV3 = 3;

    public static class CategoryListItem {
        @Nullable
        public String header;

        public boolean isCustomFunction = false;
        public String customFunctionName = null;
        public int nameResId;
        public String text;
        public @CategoryListItemType int type;

        public CategoryListItem(int nameResId, String text, @CategoryListItemType int type) {
            if (nameResId == R.string.formula_editor_function_regex_assistant) {
                this.text = "\t\t\t\t\t" + text;
            } else {
                this.text = text;
            }
            this.nameResId = nameResId;
            this.type = type;
        }
    }

    private static class AdapterItem {
        boolean isHeader;
        String headerTitle;
        CategoryListItem categoryListItem;

        AdapterItem(String headerTitle) {
            this.isHeader = true;
            this.headerTitle = headerTitle;
        }

        AdapterItem(CategoryListItem item) {
            this.isHeader = false;
            this.categoryListItem = item;
        }
    }

    private static final Set<String> globalCollapsedHeaders = new HashSet<>();
    private static boolean isFirstLaunch = true;

    private List<CategoryListItem> originalItems;
    private List<AdapterItem> visibleItems = new ArrayList<>();
    private Set<String> activeCollapsedHeaders;
    private OnItemClickListener onItemClickListener;

    public CategoryListRVAdapter(List<CategoryListItem> items) {
        this(items, false);
    }

    public CategoryListRVAdapter(List<CategoryListItem> items, boolean isSearchMode) {
        this.originalItems = items;

        if (isFirstLaunch && !isSearchMode) {
            for (CategoryListItem item : items) {
                if (item.header != null) {
                    globalCollapsedHeaders.add(item.header);
                }
            }
            isFirstLaunch = false;
        }

        if (isSearchMode) {
            this.activeCollapsedHeaders = new HashSet<>();
        } else {
            this.activeCollapsedHeaders = globalCollapsedHeaders;
        }

        buildVisibleItems();
    }

    private void buildVisibleItems() {
        visibleItems.clear();
        String currentHeader = null;
        for (CategoryListItem item : originalItems) {
            if (item.header != null) {
                currentHeader = item.header;
                visibleItems.add(new AdapterItem(currentHeader));
            }

            if (currentHeader == null || !activeCollapsedHeaders.contains(currentHeader)) {
                CategoryListItem clonedItem = new CategoryListItem(item.nameResId, item.text, item.type);
                clonedItem.isCustomFunction = item.isCustomFunction;
                clonedItem.customFunctionName = item.customFunctionName;
                clonedItem.header = null;
                visibleItems.add(new AdapterItem(clonedItem));
            }
        }
    }

    private void toggleCollapse(String header, int position) {
        boolean wasCollapsed = activeCollapsedHeaders.contains(header);

        List<AdapterItem> childrenToToggle = new ArrayList<>();
        String currentHeader = null;
        for (CategoryListItem item : originalItems) {
            if (item.header != null) {
                currentHeader = item.header;
            }
            if (header.equals(currentHeader)) {
                CategoryListItem clonedItem = new CategoryListItem(item.nameResId, item.text, item.type);
                clonedItem.isCustomFunction = item.isCustomFunction;
                clonedItem.customFunctionName = item.customFunctionName;
                clonedItem.header = null;
                childrenToToggle.add(new AdapterItem(clonedItem));
            }
        }

        int childCount = childrenToToggle.size();

        if (wasCollapsed) {
            activeCollapsedHeaders.remove(header);
            visibleItems.addAll(position + 1, childrenToToggle);
            notifyItemChanged(position);
            notifyItemRangeInserted(position + 1, childCount);
        } else {
            activeCollapsedHeaders.add(header);
            for (int i = 0; i < childCount; i++) {
                visibleItems.remove(position + 1);
            }
            notifyItemChanged(position);
            notifyItemRangeRemoved(position + 1, childCount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final AdapterItem visibleItem = visibleItems.get(position);
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;

        if (visibleItem.isHeader) {
            TextView headlineView = holder.itemView.findViewById(R.id.headline);
            if (headlineView != null) {
                headlineView.setVisibility(View.VISIBLE);
                boolean isCollapsed = activeCollapsedHeaders.contains(visibleItem.headerTitle);

                headlineView.setText(visibleItem.headerTitle.toUpperCase(Locale.getDefault()) + "   " + (isCollapsed ? "▶" : "▼"));
                headlineView.setTextSize(15);
                headlineView.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));

                headlineView.setBackgroundColor(0x0D000000);
                int paddingLeft = (int) (16 * density);
                int paddingTop = (int) (12 * density);
                headlineView.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
            }

            if (holder.title != null) {
                holder.title.setVisibility(View.GONE);
                View parentOfTitle = (View) holder.title.getParent();
                if (parentOfTitle != null && parentOfTitle != holder.itemView) {
                    parentOfTitle.setVisibility(View.GONE);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    toggleCollapse(visibleItem.headerTitle, currentPos);
                }
            });
        } else {
            final CategoryListItem item = visibleItem.categoryListItem;
            TextView headlineView = holder.itemView.findViewById(R.id.headline);
            if (headlineView != null) {
                headlineView.setVisibility(View.GONE);
            }

            if (holder.title != null) {
                holder.title.setVisibility(View.VISIBLE);
                View parentOfTitle = (View) holder.title.getParent();
                if (parentOfTitle != null && parentOfTitle != holder.itemView) {
                    parentOfTitle.setVisibility(View.VISIBLE);
                }
                holder.title.setText(item.text);
            }

            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(item);
                }
            });
        }
    }

    @Override
    public @LayoutRes int getItemViewType(int position) {
        return visibleItems.get(position).isHeader
                ? R.layout.view_holder_category_list_item_with_headline
                : R.layout.view_holder_category_list_item;
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(CategoryListItem item);
    }
}
