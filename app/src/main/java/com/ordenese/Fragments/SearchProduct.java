package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.DataSets.MenuAndItemsDataSet;
import com.ordenese.DataSets.Vendor_Info;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.R;
import com.ordenese.databinding.FragmentSearchProductBinding;

import java.util.ArrayList;

public class SearchProduct extends Fragment {

    FragmentSearchProductBinding binding;
    ArrayList<MenuAndItemsDataSet> mMenuAndChildItemsList = new ArrayList<>();
    ArrayList<MenuAndItemsDataSet> mMenuAndChildItemsListTemp = new ArrayList<>();
    Vendor_Info vendor_info;
    MenuAndItemsAdapter adapter;
    private ProgressDialog mProgressDialog;
    CartInfo cartInfo;

    public SearchProduct() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vendor_info = (Vendor_Info) getArguments().getSerializable("vendor_info");
            mMenuAndChildItemsListTemp = (ArrayList<MenuAndItemsDataSet>) getArguments().getSerializable("product_list");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(AppLanguageSupport.onAttach(context));
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setLayoutDirection(
                    "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                            View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        }
        cartInfo = (CartInfo) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchProductBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        for (int i = 0; i < mMenuAndChildItemsListTemp.size(); i++) {
            if (!mMenuAndChildItemsListTemp.get(i).getParentMenu()) {
                mMenuAndChildItemsList.add(mMenuAndChildItemsListTemp.get(i));
            }
        }

        binding.productList.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        adapter = new MenuAndItemsAdapter(getActivity(), mMenuAndChildItemsList);
        binding.productList.setAdapter(adapter);

        binding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.searchText.getText().toString().isEmpty()) {
                    binding.searchText.setText("");
                    mProgressDialog.show();
                    adapter = new MenuAndItemsAdapter(getActivity(), mMenuAndChildItemsList);
                    binding.productList.setAdapter(adapter);
                    mProgressDialog.cancel();
                }
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mProgressDialog.show();
                if (s.length() != 0) {
                    ArrayList<MenuAndItemsDataSet> mItemsListTemp = new ArrayList<>();
                    for (int i = 0; i < mMenuAndChildItemsList.size(); i++) {
                        if (mMenuAndChildItemsList.get(i).getChildName().toLowerCase().contains(s.toString().toLowerCase())) {
                            mItemsListTemp.add(mMenuAndChildItemsList.get(i));
                        }
                    }
                    adapter = new MenuAndItemsAdapter(getActivity(), mItemsListTemp);
                } else {
                    adapter = new MenuAndItemsAdapter(getActivity(), mMenuAndChildItemsList);
                }
                binding.productList.setAdapter(adapter);

                mProgressDialog.cancel();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mProgressDialog.cancel();

        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            for (int i = 0; i < mMenuAndChildItemsListTemp.size(); i++) {
                if (!mMenuAndChildItemsListTemp.get(i).getParentMenu()) {
                    mMenuAndChildItemsList.add(mMenuAndChildItemsListTemp.get(i));
                }
            }

            binding.productList.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
            adapter = new MenuAndItemsAdapter(getActivity(), mMenuAndChildItemsList);
            binding.productList.setAdapter(adapter);
        }
        cartInfo.cart_info(false, "", "");
    }

    public class MenuAndItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final ArrayList<MenuAndItemsDataSet> mAdapterMenuAndChildItemsList;

        private final Context mContext;

        public MenuAndItemsAdapter(Context context, ArrayList<MenuAndItemsDataSet> menuAndChildItemsList) {
            this.mContext = context;
            this.mAdapterMenuAndChildItemsList = menuAndChildItemsList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 2) {
                return new MenuViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.search_product_list, parent, false));
            } else {
                return new MenuListEmptyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.store_menu_list_empty, parent, false));
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder.getItemViewType() == 2) {
                MenuViewHolder menuViewHolder = (MenuViewHolder) holder;

                menuViewHolder.mChildMenuItemsContainer.setVisibility(View.VISIBLE);

                menuViewHolder.mChildItemName.setText(mAdapterMenuAndChildItemsList.get(position).getChildName());
                menuViewHolder.mChildItemDescription.setText(mAdapterMenuAndChildItemsList.get(position).getChildProductDescription());

                if (mAdapterMenuAndChildItemsList.get(position).getPrice_status().equals("1")) {
                    menuViewHolder.mChildItemPrice.setVisibility(View.VISIBLE);

                    //To check offer price available or not :-
                    if (!mAdapterMenuAndChildItemsList.get(position).getChildOfferPrice().isEmpty()) {
                        menuViewHolder.temp_price.setVisibility(View.VISIBLE);
                        menuViewHolder.temp_price.setText(mAdapterMenuAndChildItemsList.get(position).getChildPrice());
                        menuViewHolder.temp_price.setPaintFlags(menuViewHolder.temp_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        menuViewHolder.mChildItemPrice.setText(mAdapterMenuAndChildItemsList.get(position).getChildOfferPrice());
                    } else {
                        menuViewHolder.temp_price.setVisibility(View.GONE);
                        menuViewHolder.temp_price.setPaintFlags(0);
                        menuViewHolder.mChildItemPrice.setText(mAdapterMenuAndChildItemsList.get(position).getChildPrice());
                    }

                } else {
                    menuViewHolder.temp_price.setVisibility(View.VISIBLE);
                    menuViewHolder.temp_price.setText(mContext.getResources().getString(R.string.price_on_selection));
                    menuViewHolder.mChildItemPrice.setVisibility(View.GONE);
                }

                AppFunctions.imageLoaderUsingGlide(mAdapterMenuAndChildItemsList.get(position).getChildLogo(), menuViewHolder.mProductImage,getActivity());

                //To remove the last item bottom line :-
//                    int mTempPosition = position + 1;
//                    if (mTempPosition < mAdapterMenuAndChildItemsList.size() - 1) {
//                        if (mAdapterMenuAndChildItemsList.get(mTempPosition).getParentMenu()) {
//                            menuViewHolder.mChildBottomLine.setVisibility(View.GONE);
//                        } else {
//                            menuViewHolder.mChildBottomLine.setVisibility(View.VISIBLE);
//                        }
//                    }

                menuViewHolder.mChildMenuItemsContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mContext != null) {


                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                            ProductDetails productDetails = new ProductDetails();
                            Bundle mBundle = new Bundle();
                            mBundle.putString("vendor_id", vendor_info.getVendor().getVendorId());
                            mBundle.putString("vendor_name", vendor_info.getVendor().getName());

                            mBundle.putString("vendor_status", vendor_info.getVendor().getVendorStatus());


                            mBundle.putString("product_id", mAdapterMenuAndChildItemsList.get(position).getChildProductId());
                            mBundle.putString("product_name", mAdapterMenuAndChildItemsList.get(position).getChildName());
                            mBundle.putString("category_id", mAdapterMenuAndChildItemsList.get(position).getParentSectionId());
                            mBundle.putString("latitude", vendor_info.getVendor().getLatitude());
                            mBundle.putString("longitude", vendor_info.getVendor().getLongitude());
                            mBundle.putString("product_desc", mAdapterMenuAndChildItemsList.get(position).getChildProductDescription());
                            mBundle.putString("product_image", mAdapterMenuAndChildItemsList.get(position).getChildImage());
                            mBundle.putString("product_minimum", mAdapterMenuAndChildItemsList.get(position).getChildProductMinQty());
                            mBundle.putString("product_price", mAdapterMenuAndChildItemsList.get(position).getChildPrice());
                            mBundle.putString("price_status", mAdapterMenuAndChildItemsList.get(position).getPrice_status());
                            mBundle.putSerializable("product_details", mAdapterMenuAndChildItemsList.get(position).getProductOptionList());
                            productDetails.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, productDetails, "productDetails");
                            mFT.addToBackStack("productDetails");
                            mFT.commit();
                        }
                    }
                });
            }

        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {
            if (mAdapterMenuAndChildItemsList != null) {
                if (mAdapterMenuAndChildItemsList.size() > 0) {
                    return mAdapterMenuAndChildItemsList.size();
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mAdapterMenuAndChildItemsList != null) {
                if (mAdapterMenuAndChildItemsList.size() > 0) {
                    return 2;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }

        }

        public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            View mChildBottomLine;

            TextView mChildItemName;
            TextView mChildItemDescription;
            TextView mChildItemPrice, temp_price;
            private ImageView mProductImage;
            LinearLayout mChildMenuItemsContainer;

            public MenuViewHolder(View itemView) {
                super(itemView);

                mChildItemName = itemView.findViewById(R.id.product_name);
                mChildItemDescription = itemView.findViewById(R.id.product_desc);
                mChildItemPrice = itemView.findViewById(R.id.product_price);
                temp_price = itemView.findViewById(R.id.temp_price);
                mChildBottomLine = itemView.findViewById(R.id.view_bottom);
                mProductImage = itemView.findViewById(R.id.mProductImage);
                mChildMenuItemsContainer = itemView.findViewById(R.id.product_container_linear);

            }

            @Override
            public void onClick(View v) {

            }
        }

        public class MenuListEmptyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout menuListEmptyUI;

            MenuListEmptyViewHolder(View itemView) {
                super(itemView);
                menuListEmptyUI = itemView.findViewById(R.id.layout_restaurant_menu_item_list_empty_message);
            }
        }

    }

}