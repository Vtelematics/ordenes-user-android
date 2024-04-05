package com.ordenese.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.GroceryCategoryDataSet;
import com.ordenese.DataSets.GroceryInfoApi;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.MakeBottomMarginForViewBasket;
import com.ordenese.R;
import com.ordenese.databinding.GroceryCategoryMainPageBinding;
import com.ordenese.databinding.GroceryCmpBottomBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroceryCategoryMainPage extends Fragment implements View.OnClickListener {

    private GroceryCategoryMainPageBinding mGCMPBinding;
    private ProgressDialog mProgressDialog;
    private RecyclerView.LayoutManager mLayoutMgrGCMPParent;
    private GCMPAdapter mGcMpAdapter;

    private String mStore_ID = "", mStore_NAME = "";
    private ArrayList<Object> mGCMPParentList;

    private RetrofitInterface retrofitInterface;
    public GroceryInfoApi mGroceryInfoApi;

    Activity activity;
    private CartInfo cartInfo;
    private MakeBottomMarginForViewBasket mMakeBottomMarginForViewBasket;

    public GroceryCategoryMainPage() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(AppLanguageSupport.onAttach(context));
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setLayoutDirection(
                    "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                            View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
        activity = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.grocery_category_main_page, container, false);
        mGCMPBinding = GroceryCategoryMainPageBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        if (getArguments() != null) {
            mStore_ID = getArguments().getString(DefaultNames.store_id);
            mStore_NAME = getArguments().getString(DefaultNames.store_name);
        }

        cartInfo = (CartInfo) getActivity();
        mMakeBottomMarginForViewBasket = (MakeBottomMarginForViewBasket) getActivity();
        mGCMPBinding.imgGcMpBack.setOnClickListener(this);
        mGCMPBinding.recyclerGcMpParent.setVisibility(View.VISIBLE);

        mGCMPBinding.deliveryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGCMPBinding.deliveryTv.setBackground(activity.getResources().getDrawable(R.drawable.bg_white_curve));
                mGCMPBinding.pickupTv.setBackground(null);

                if (!OrderTypeDB.getInstance(activity).getUserServiceType().isEmpty()) {
                    OrderTypeDB.getInstance(activity).updateUserServiceType("1");
                } else {
                    OrderTypeDB.getInstance(activity).addUserServiceType("1");
                }
            }
        });

        mGCMPBinding.pickupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGCMPBinding.pickupTv.setBackground(activity.getResources().getDrawable(R.drawable.bg_white_curve));
                mGCMPBinding.deliveryTv.setBackground(null);

                if (!OrderTypeDB.getInstance(activity).getUserServiceType().isEmpty()) {
                    OrderTypeDB.getInstance(activity).updateUserServiceType("2");
                } else {
                    OrderTypeDB.getInstance(activity).addUserServiceType("2");
                }
            }
        });

        return mGCMPBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
        if (mId == R.id.img_gc_mp_back) {
            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (OrderTypeDB.getInstance(activity).getUserServiceType().equals("2")) {
            mGCMPBinding.pickupTv.setBackground(activity.getResources().getDrawable(R.drawable.bg_white_curve));
            mGCMPBinding.deliveryTv.setBackground(null);
        } else {
            mGCMPBinding.deliveryTv.setBackground(activity.getResources().getDrawable(R.drawable.bg_white_curve));
            mGCMPBinding.pickupTv.setBackground(null);
        }

        get_vendor_info();
        cartInfo.cart_info(false, "", "");
        cart_product_count();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
        cartInfo.cart_info(false, "", "");
    }

    private void cart_product_count() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                mProgressDialog.show();

                try {
                    retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    Call<String> Call = retrofitInterface.cart_product_count(mCustomerAuthorization, body);
                    Call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            mProgressDialog.cancel();
                            try {
                                JSONObject object = new JSONObject(response.body());
                                if (!object.isNull("qty_count")) {
                                    if (!object.getString("qty_count").equals("0")) {
                                        if (!object.isNull("total")) {
                                            mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(true);
                                            cartInfo.cart_info(true, object.getString("qty_count"), object.toString());
                                        } else {
                                            mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                                        }
                                    } else {
                                        mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                                        cartInfo.cart_info(false, "", "");
                                    }
                                } else {
                                    mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                                }
                            } catch (JSONException e) {
                                mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                            mProgressDialog.cancel();
                        }
                    });
                } catch (Exception e) {
                    mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                }


            } else {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_home_restaurant_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }

    }

    public class GCMPAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<Object> mGCMP_Parent_List;

        public GCMPAdapter(ArrayList<Object> gCMP_Parent_List) {
            this.mGCMP_Parent_List = gCMP_Parent_List;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                // for search and delivery details ui.
                return new TopViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.gc_mp_top_ui, parent, false));
            } else if (viewType == 1) {
                // for category list ui.
                return new CenterViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.gc_mp_center_ui, parent, false));
            } else {
                // for products list ui.
                //its viewType == 2
                return new BottomViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.gc_mp_bottom_ui, parent, false));
            }
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            if (holder.getItemViewType() == 0) {
                // for search and delivery details ui.

                //Log.e("TopViewHolder","called");

                TopViewHolder mTopVHolder = (TopViewHolder) holder;

                mTopVHolder.mLaySearchGrocery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                        GroceryAllFeaturedProducts m_groceryAllFeaturedProducts = new GroceryAllFeaturedProducts();
                        Bundle bundle = new Bundle();
                        bundle.putString(DefaultNames.from, DefaultNames.callFromGroceryCategory);
                        bundle.putString(DefaultNames.store_id, mStore_ID);
                        m_groceryAllFeaturedProducts.setArguments(bundle);
                        mFT.replace(R.id.layout_app_home_body, m_groceryAllFeaturedProducts, "m_groceryAllFeaturedProducts");
                        mFT.addToBackStack("m_groceryAllFeaturedProducts");
                        mFT.commit();

                    }
                });

                if (getActivity() != null) {
                    String mDTime = mGroceryInfoApi.storeInfo.delivery_time
                            + " " + getActivity().getResources().getString(R.string.mins);
                    mTopVHolder.mDeliveryTime.setText(mDTime);
                } else {
                    mTopVHolder.mDeliveryTime.setText("");
                }

                mTopVHolder.mDelivery.setText(mGroceryInfoApi.storeInfo.delivery_charge);
                mTopVHolder.mMinOrder.setText(mGroceryInfoApi.storeInfo.minimum_amount);

            } else if (holder.getItemViewType() == 1) {
                // for category list ui.

                //    Log.e("CenterViewHolder","called");

                CenterViewHolder mCenterVHolder = (CenterViewHolder) holder;

                if (mGroceryInfoApi.categoryList != null && mGroceryInfoApi.categoryList.size() > 0) {

                    //  Log.e("CenterViewHolder","data");

                    mCenterVHolder.mListEmptyLay.setVisibility(View.GONE);
                    mCenterVHolder.recyclerCategoryList.setVisibility(View.VISIBLE);
                    mCenterVHolder.mTvShopByCategoryTitle.setVisibility(View.VISIBLE);

                    mCenterVHolder.mCategoryListMgr = new GridLayoutManager(getActivity(), 4);
                    mCenterVHolder.recyclerCategoryList.setLayoutManager(mCenterVHolder.mCategoryListMgr);
                    ArrayList<GroceryCategoryDataSet> mTempList = new ArrayList<>();
                    if (mGroceryInfoApi.categoryList.size() > 11) {

                        for (int category = 0; category < 12; category++) {
                            GroceryCategoryDataSet mGCategoryDs = mGroceryInfoApi.categoryList.get(category);
                            mTempList.add(mGCategoryDs);
                        }

                        mCenterVHolder.mCategoryListAdapter = new CategoryListAdapter(mTempList, true);
                        mCenterVHolder.recyclerCategoryList.setAdapter(mCenterVHolder.mCategoryListAdapter);
                    } else {
                        mTempList = mGroceryInfoApi.categoryList;
                        mCenterVHolder.mCategoryListAdapter = new CategoryListAdapter(mTempList, false);
                        mCenterVHolder.recyclerCategoryList.setAdapter(mCenterVHolder.mCategoryListAdapter);
                    }

                } else {

                    //  Log.e("CenterViewHolder","data empty");

                    mCenterVHolder.mListEmptyLay.setVisibility(View.VISIBLE);
                    mCenterVHolder.recyclerCategoryList.setVisibility(View.GONE);
                    mCenterVHolder.mTvShopByCategoryTitle.setVisibility(View.GONE);
                }

            } /*else {
                // for products list ui.

                // Log.e("BottomViewHolder","called");

                BottomViewHolder mBottomVHolder = (BottomViewHolder) holder;

                ArrayList<String> categoryList = new ArrayList<>();
                categoryList.add("Imported for you");
                categoryList.add("Summer Selection");
                categoryList.add("Promo Packs");
                categoryList.add("Every day Roastery Coffee");

                categoryList.add("Imported for you");
                categoryList.add("Summer Selection");
                categoryList.add("Promo Packs");
                categoryList.add("Every day Roastery Coffee");

                categoryList.add("Imported for you");
                categoryList.add("Summer Selection");
                categoryList.add("Promo Packs");
                categoryList.add("Every day Roastery Coffee");

                categoryList.add("Imported for you");
                categoryList.add("Summer Selection");
                categoryList.add("Promo Packs");
                categoryList.add("Every day Roastery Coffee");

                if (categoryList != null && categoryList.size() > 0) {

                    mBottomVHolder.mProductListMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                    mBottomVHolder.recyclerProductList.setLayoutManager(mBottomVHolder.mProductListMgr);
                    mBottomVHolder.mProductListAdapter = new ProductListAdapter(categoryList, false);
                    mBottomVHolder.recyclerProductList.setAdapter(mBottomVHolder.mProductListAdapter);

                }

                mBottomVHolder.mTvTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                        GroceryAllFeaturedProducts m_groceryAllFeaturedProducts = new GroceryAllFeaturedProducts();
                        Bundle bundle = new Bundle();
                        bundle.putString(DefaultNames.from, DefaultNames.callFromFeatureProducts);
                        bundle.putString(DefaultNames.store_id, mStore_ID);
                        m_groceryAllFeaturedProducts.setArguments(bundle);
                        mFT.replace(R.id.layout_app_home_body, m_groceryAllFeaturedProducts, "m_groceryAllFeaturedProducts");
                        mFT.addToBackStack("m_groceryAllFeaturedProducts");
                        mFT.commit();

                    }
                });


            }*/

        }


        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }


        @Override
        public int getItemCount() {
            return mGCMP_Parent_List.size();
        }

        @Override
        public int getItemViewType(int position) {

            // for search and delivery details ui - 0 - true
            // for category list ui - 1 - arrayList
            // for products list ui - 2 - String

            if (mGCMP_Parent_List.get(position) instanceof Boolean) {
                // for search and delivery details ui.
                //  Log.e("getItemViewType","1");
                return 0;
            } else if (mGCMP_Parent_List.get(position) instanceof ArrayList) {
                // for category list ui.
                //   Log.e("getItemViewType","2");
                return 1;
            } else {
                // for products list ui.
                // Log.e("getItemViewType","0");
                return 2;
            }


        }


        public class BottomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView mTvTitle;
            private RecyclerView recyclerProductList;
            private RecyclerView.LayoutManager mProductListMgr;
            private ProductListAdapter mProductListAdapter;


            public BottomViewHolder(View itemView) {
                super(itemView);

                mTvTitle = itemView.findViewById(R.id.tv_gc_mp_bu_shop_by_product_title);
                recyclerProductList = itemView.findViewById(R.id.recycler_gc_mp_bu_product_list);


            }

            @Override
            public void onClick(View v) {

            }
        }


        public class TopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private LinearLayout mLaySearchGrocery;
            private LinearLayout mLaySearchItemClear;
            private TextView mDeliveryTime, mDelivery, mMinOrder;

            public TopViewHolder(View itemView) {
                super(itemView);

                mLaySearchGrocery = itemView.findViewById(R.id.lay_gc_mp_tu_search_grocery);

                mDeliveryTime = itemView.findViewById(R.id.tv_gc_mp_tu_delivery_time);
                mDelivery = itemView.findViewById(R.id.tv_gc_mp_tu_delivery);
                mMinOrder = itemView.findViewById(R.id.tv_gc_mp_tu_min_order);


            }

            @Override
            public void onClick(View v) {

            }
        }

        public class CenterViewHolder extends RecyclerView.ViewHolder {

            private TextView mTvShopByCategoryTitle;
            private RecyclerView recyclerCategoryList;
            private RecyclerView.LayoutManager mCategoryListMgr;
            private CategoryListAdapter mCategoryListAdapter;
            private LinearLayout mListEmptyLay;


            CenterViewHolder(View itemView) {
                super(itemView);

                mTvShopByCategoryTitle = itemView.findViewById(R.id.tv_gc_mp_cu_shop_by_category_title);
                recyclerCategoryList = itemView.findViewById(R.id.recycler_gc_mp_cu_category_list);
                mListEmptyLay = itemView.findViewById(R.id.lay_gc_mp_data_empty);


            }
        }


        public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {

            private ArrayList<GroceryCategoryDataSet> m_Category_List;
            private LinearLayout mListEmptyContainer;
            private RecyclerView mListView;
            private Boolean mIsGreaterThan_11_items;


            public CategoryListAdapter(ArrayList<GroceryCategoryDataSet> category_List, Boolean isGreaterThan_11_items) {
                this.m_Category_List = category_List;
                this.mIsGreaterThan_11_items = isGreaterThan_11_items;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.grocery_category_list_row, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
                //  ////Log.e("CartProductListAdapter onBindViewHolder","");


                //AppFunctions.imageLoaderUsingGlide(m_Cart_List.get(position).getImage(), holder.mVendorImg, getActivity());


                if (getActivity() != null) {

                    if (position <= 11) {
                        holder.mLayRow.setVisibility(View.VISIBLE);
                        if (position == 11) {
                            if (mIsGreaterThan_11_items) {
                                holder.mCategoryName.setText(getActivity().getResources().getString(R.string.gc_mp_view_all));
                                holder.mCategoryImg.setVisibility(View.GONE);
                                holder.mCategoryImgMoreThan11.setVisibility(View.VISIBLE);
                                Glide.with(getActivity()).load(R.drawable.svg_grid_view_48dp).into(holder.mCategoryImgMoreThan11);
                            } else {
                                holder.mCategoryName.setText(m_Category_List.get(position).name);
                                holder.mCategoryImg.setVisibility(View.VISIBLE);
                                holder.mCategoryImgMoreThan11.setVisibility(View.GONE);
                                Glide.with(getActivity()).load(m_Category_List.get(position).picture).into(holder.mCategoryImg);
                            }
                        } else {
                            holder.mCategoryName.setText(m_Category_List.get(position).name);
                            holder.mCategoryImg.setVisibility(View.VISIBLE);
                            holder.mCategoryImgMoreThan11.setVisibility(View.GONE);
                            Glide.with(getActivity()).load(m_Category_List.get(position).picture).into(holder.mCategoryImg);
                        }
                    } else {
                        holder.mLayRow.setVisibility(View.GONE);
                    }

                }

                holder.mLayRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (position == 11) {
                            if (mIsGreaterThan_11_items) {

                                //AppFunctions.toastShort(getActivity(), "More than 11 items");
                                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                CategoriesListBottom categoriesListBottom = new CategoriesListBottom(mGroceryInfoApi, mStore_ID, mStore_NAME);
                                categoriesListBottom.show(getParentFragmentManager(), "categoriesListBottom");


                            } else {
                                // AppFunctions.toastShort(getActivity(), m_Category_List.get(position).name);
                                toPerformCategoryProductCall(position);
                            }
                        } else {
                            //AppFunctions.toastShort(getActivity(), m_Category_List.get(position).name);
                            toPerformCategoryProductCall(position);
                        }

                    }
                });

                /*if (position == m_Cart_List.size() - 1) {
                    //To visible the horizontal line only when after the last item of cart list :-
                    holder.mViewLine.setVisibility(View.VISIBLE);
                } else {
                    holder.mViewLine.setVisibility(View.GONE);
                }*/


            }

            private void toPerformCategoryProductCall(int currentPosition) {

                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                GroceryProductListing groceryProductListing = new GroceryProductListing();
                Bundle mBundle = new Bundle();
                mBundle.putString("category_id", m_Category_List.get(currentPosition).category_id);
                mBundle.putString("vendor_id", mStore_ID);
                mBundle.putString("vendor_name", mStore_NAME);

                mBundle.putString(DefaultNames.vendor_latitude, "");
                mBundle.putString(DefaultNames.vendor_longitude, "");

                mBundle.putString("vendor_status", mGroceryInfoApi.storeInfo.vendor_status);
                mBundle.putInt("category_position", currentPosition);
                mBundle.putSerializable("category_list", m_Category_List);
                groceryProductListing.setArguments(mBundle);
                mFT.replace(R.id.layout_app_home_body, groceryProductListing, "groceryProductListing");
                mFT.addToBackStack("groceryProductListing");
                mFT.commit();

            }


            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public int getItemCount() {
                return m_Category_List.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


                private TextView mCategoryName;
                private ImageView mCategoryImg, mCategoryImgMoreThan11;
                private LinearLayout mLayRow;

                public ViewHolder(View itemView) {
                    super(itemView);

                    mCategoryName = itemView.findViewById(R.id.tv_gc_l_category_name);

                    mCategoryImgMoreThan11 = itemView.findViewById(R.id.iv_gc_l_category_image_more_than_11);
                    mCategoryImg = itemView.findViewById(R.id.iv_gc_l_category_image);
                    mLayRow = itemView.findViewById(R.id.lay_gc_list_row);


                }

                @Override
                public void onClick(View v) {

                }
            }
        }

        public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

            private ArrayList<String> m_Category_List;
            private LinearLayout mListEmptyContainer;
            private RecyclerView mListView;
            private Boolean mIsGreaterThan_11_items;


            public ProductListAdapter(ArrayList<String> category_List, Boolean isGreaterThan_11_items) {
                this.m_Category_List = category_List;
                this.mIsGreaterThan_11_items = isGreaterThan_11_items;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.grocery_cmp_product_list, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
                //  ////Log.e("CartProductListAdapter onBindViewHolder","");

                holder.mCategoryName.setText(m_Category_List.get(position));
                //AppFunctions.imageLoaderUsingGlide(m_Cart_List.get(position).getImage(), holder.mVendorImg, getActivity());


                if (position == 2 || position == 4) {
                    holder.mPrice.setVisibility(View.VISIBLE);
                    holder.mSpecialPrice.setText("$ 20");
                    holder.mPrice.setText("$ 50");
                    holder.mPrice.setPaintFlags(holder.mPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.mSpecialPrice.setText("$ 20");
                    holder.mPrice.setVisibility(View.GONE);
                }


                if (getActivity() != null) {
                    Glide.with(getActivity()).load(R.drawable.x_fruit_category).into(holder.mCategoryImg);
                }

                holder.mLayRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppFunctions.toastShort(getActivity(), m_Category_List.get(position));

                    }
                });

                /*if (position == m_Cart_List.size() - 1) {
                    //To visible the horizontal line only when after the last item of cart list :-
                    holder.mViewLine.setVisibility(View.VISIBLE);
                } else {
                    holder.mViewLine.setVisibility(View.GONE);
                }*/


            }


            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public int getItemCount() {

                if (m_Category_List.size() > 4) {
                    return 4;
                } else {
                    return m_Category_List.size();
                }

            }

            public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


                private TextView mCategoryName, mSpecialPrice, mPrice;
                private ImageView mCategoryImg;
                private LinearLayout mLayRow;

                public ViewHolder(View itemView) {
                    super(itemView);

                    mSpecialPrice = itemView.findViewById(R.id.tv_gc_l_pl_special_price);
                    mPrice = itemView.findViewById(R.id.tv_gc_l_pl_price);

                    mCategoryName = itemView.findViewById(R.id.tv_gc_l_pl_name);
                    mCategoryImg = itemView.findViewById(R.id.iv_gc_l_pl_image);
                    mLayRow = itemView.findViewById(R.id.lay_gc_list_pl_row);


                }

                @Override
                public void onClick(View v) {

                }
            }
        }



/*
        public class CartTotalsListAdapter extends RecyclerView.Adapter<CartTotalsListAdapter.ViewHolder> {

            private ArrayList<CartTotalsDataSet> mTotalsList;

            public void toUpdateCartTotalsList(ArrayList<CartTotalsDataSet> totalsList) {
                this.mTotalsList = totalsList;
            }

            public CartTotalsListAdapter(ArrayList<CartTotalsDataSet> totalsList) {
                this.mTotalsList = totalsList;
            }

            @Override
            public CartTotalsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.cart_totals_row, parent, false);
                return new CartTotalsListAdapter.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final CartTotalsListAdapter.ViewHolder holder, final int position) {
                holder.mCartTotalsTitle.setText(mTotalsList.get(position).getTitle());
                holder.mCartTotalsData.setText(mTotalsList.get(position).getText());

                //To make Total field only bold and remaining fields are non bold:-
                String mTitleKey = mTotalsList.get(position).getTitle_key();

                if (mTitleKey.equals("total")) {

                    holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.BOLD);
                    holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.BOLD);

                    holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));
                    holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));

                } else if (mTitleKey.equals("offer")) {

                    holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                    holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);

                    holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                    holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));


                } else if (mTitleKey.equals("coupon")) {

                    holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                    holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);

                    holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                    holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));

                } else {

                    holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                    holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);

                    holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));
                    holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));

                }


            }

            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public int getItemCount() {
                return mTotalsList.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder {

                TextView mCartTotalsTitle, mCartTotalsData;

                public ViewHolder(View itemView) {
                    super(itemView);

                    mCartTotalsTitle = itemView.findViewById(R.id.tv_cl_total_title);
                    mCartTotalsData = itemView.findViewById(R.id.tv_cl_total_data);

                }

            }
        }
*/


    }

    private void toHideDeviceKeyboard() {
        if (getActivity() != null) {
            if (mGCMPBinding != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mGCMPBinding.getRoot();
                // To get the window token when there is no currently focused view, we have a to create a view
                if (v__iew == null) {
                    v__iew = new View(getActivity());
                }
                // hide the keyboard
                imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);
            }
        }
    }

    public static class CategoriesListBottom extends BottomSheetDialogFragment {

        GroceryCmpBottomBinding binding;
        GroceryInfoApi m_GroceryInfoApi;
        String m__StoreID, m__StoreNAME;

        public CategoriesListBottom(GroceryInfoApi groceryInfoApi, String storeID, String storeNAME) {
            // Required empty public constructor
            this.m_GroceryInfoApi = groceryInfoApi;
            this.m__StoreID = storeID;
            this.m__StoreNAME = storeNAME;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
            if (getArguments() != null) {

            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            binding = GroceryCmpBottomBinding.inflate(inflater, container, false);
            binding.recyclerGCmpBList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
            binding.recyclerGCmpBList.setAdapter(new AllCategoryListAdapter(m_GroceryInfoApi.categoryList));
            binding.imgGCmpBCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getDialog() != null) {
                        getDialog().dismiss();
                    }
                }
            });

            return binding.getRoot();
        }

        public class AllCategoryListAdapter extends RecyclerView.Adapter<AllCategoryListAdapter.ViewHolder> {

            private ArrayList<GroceryCategoryDataSet> m_Category_List;
            private LinearLayout mListEmptyContainer;
            private RecyclerView mListView;


            public AllCategoryListAdapter(ArrayList<GroceryCategoryDataSet> category_List) {
                this.m_Category_List = category_List;

            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.grocery_category_list_row, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
                //  ////Log.e("CartProductListAdapter onBindViewHolder","");

                holder.mCategoryName.setText(m_Category_List.get(position).name);
                //AppFunctions.imageLoaderUsingGlide(m_Cart_List.get(position).getImage(), holder.mVendorImg, getActivity());


                if (getActivity() != null) {
                    holder.mCategoryImg.setVisibility(View.VISIBLE);
                    holder.mCategoryImgMoreThan11.setVisibility(View.GONE);
                    Glide.with(getActivity()).load(m_Category_List.get(position).picture).into(holder.mCategoryImg);
                }

                holder.mLayRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //AppFunctions.toastShort(getActivity(), m_Category_List.get(position).name);
                        toPerformCategoryProductCall(position);
                        getDialog().dismiss();
                    }
                });


            }

            private void toPerformCategoryProductCall(int currentPosition) {

//                Log.e("toPerformCategory", currentPosition + "");

                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                GroceryProductListing groceryProductListing = new GroceryProductListing();
                Bundle mBundle = new Bundle();
                mBundle.putString("category_id", m_Category_List.get(currentPosition).category_id);
                mBundle.putString("vendor_id", m__StoreID);
                mBundle.putString("vendor_name", m__StoreNAME);

                mBundle.putString(DefaultNames.vendor_latitude, "");
                mBundle.putString(DefaultNames.vendor_longitude, "");

                mBundle.putString("vendor_status", m_GroceryInfoApi.storeInfo.vendor_status);
                mBundle.putInt("category_position", currentPosition);
                mBundle.putSerializable("category_list", m_Category_List);
                groceryProductListing.setArguments(mBundle);
                mFT.replace(R.id.layout_app_home_body, groceryProductListing, "groceryProductListing");
                mFT.addToBackStack("groceryProductListing");
                mFT.commit();

            }


            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public int getItemCount() {
                return m_Category_List.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


                private TextView mCategoryName;
                private ImageView mCategoryImg, mCategoryImgMoreThan11;
                private LinearLayout mLayRow;

                public ViewHolder(View itemView) {
                    super(itemView);

                    mCategoryName = itemView.findViewById(R.id.tv_gc_l_category_name);

                    mCategoryImgMoreThan11 = itemView.findViewById(R.id.iv_gc_l_category_image_more_than_11);
                    mCategoryImg = itemView.findViewById(R.id.iv_gc_l_category_image);
                    mLayRow = itemView.findViewById(R.id.lay_gc_list_row);


                }

                @Override
                public void onClick(View v) {

                }
            }
        }


       /* private void setupRatio(BottomSheetDialog bottomSheetDialog) {
            //id = com.google.android.material.R.id.design_bottom_sheet for Material Components
            //id = android.support.design.R.id.design_bottom_sheet for support librares
            FrameLayout bottomSheet = (FrameLayout)
                    bottomSheetDialog.findViewById(R.id.design_bottom_sheet);

            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
            layoutParams.height = getBottomSheetDialogDefaultHeight();
            bottomSheet.setLayoutParams(layoutParams);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        private int getBottomSheetDialogDefaultHeight() {
            return getWindowHeight() * 85 / 100;
        }
        private int getWindowHeight() {
            // Calculate window height for fullscreen use
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }*/

    }

    private void get_vendor_info() {

        if (getActivity() != null) {

            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                String latitude = "", longitude = "";

                if (AreaGeoCodeDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    AreaGeoCodeDataSet mAreaGeoCodeDS = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                    latitude = mAreaGeoCodeDS.getmLatitude();
                    longitude = mAreaGeoCodeDS.getmLongitude();
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.vendor_id, mStore_ID);
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    //To pass vendor_type_id as 2 for grocery business type :-
                    jsonObject.put(DefaultNames.vendor_type_id, "2");
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put(DefaultNames.latitude, latitude);
                    jsonObject.put(DefaultNames.longitude, longitude);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GroceryInfoApi> Call = retrofitInterface.groceryInfo(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<GroceryInfoApi>() {
                        @Override
                        public void onResponse(@NonNull Call<GroceryInfoApi> call, @NonNull Response<GroceryInfoApi> response) {

                            if (getActivity() != null) {
                                mProgressDialog.cancel();
                                if (response.isSuccessful()) {
                                    mGroceryInfoApi = response.body();
                                    if (mGroceryInfoApi != null) {
                                        if (mGroceryInfoApi.success != null) {
                                            mStore_NAME = mGroceryInfoApi.storeInfo.vendor_name;
                                            mGCMPBinding.tvGcMpAppBarTitle.setText(mStore_NAME);

                                            String ab = activity.getResources().getString(R.string.in) + " " + mGroceryInfoApi.storeInfo.delivery_time + " " + activity.getResources().getString(R.string.mins_) +
                                                    " - " + mGroceryInfoApi.storeInfo.delivery_charge;
                                            mGCMPBinding.deliveryInfo.setText(ab);
                                            String cd = activity.getResources().getString(R.string.in) + " " + mGroceryInfoApi.storeInfo.preparing_time + " " + activity.getResources().getString(R.string.mins_) +
                                                    " - " + mGroceryInfoApi.storeInfo.vendor_distance + " " + activity.getResources().getString(R.string.km);
                                            mGCMPBinding.pickupInfo.setText(cd);
                                            mGCMPBinding.orderTypeLinear.setVisibility(View.VISIBLE);
                                            if (mGroceryInfoApi.storeInfo.delivery.equals("1") && mGroceryInfoApi.storeInfo.pick_up.equals("1")) {
                                                mGCMPBinding.deliveryTv.setVisibility(View.VISIBLE);
                                                mGCMPBinding.pickupTv.setVisibility(View.VISIBLE);
                                            } else {
                                                if (mGroceryInfoApi.storeInfo.pick_up.equals("0")) {
                                                    mGCMPBinding.deliveryTv.setVisibility(View.VISIBLE);
                                                    mGCMPBinding.pickupTv.setVisibility(View.GONE);
                                                }
                                                if (mGroceryInfoApi.storeInfo.delivery.equals("0")) {
                                                    mGCMPBinding.deliveryTv.setVisibility(View.GONE);
                                                    mGCMPBinding.pickupTv.setVisibility(View.VISIBLE);
                                                }
                                            }

                                            //Api response successDataSet :-
                                            mGCMPParentList = new ArrayList<>();
                                            mGCMPParentList.add(true); // for search and delivery details ui.
                                            /*if (mGroceryInfoApi.categoryList != null && mGroceryInfoApi.categoryList.size() > 0) {
                                                mGCMPParentList.add(mGroceryInfoApi.categoryList); // for category list ui.
                                                //  mGCMPParentList.add(DefaultNames.product_list); // for products list ui.
                                            } else {
                                            }*/
                                            mGCMPParentList.add(mGroceryInfoApi.categoryList);

                                            mLayoutMgrGCMPParent = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                            mGCMPBinding.recyclerGcMpParent.setLayoutManager(mLayoutMgrGCMPParent);
                                            mGcMpAdapter = new GCMPAdapter(mGCMPParentList);
                                            mGCMPBinding.recyclerGcMpParent.setAdapter(mGcMpAdapter);


                                            /*mGCMPBinding.resName.setText(mVendorInfoApi.getVendor().getName());
                                                AppFunctions.imageLoaderUsingGlide(mVendorInfoApi.getVendor().getLogo(), binding.resImage,getActivity());
                                                StringBuilder mCuisine = new StringBuilder();
                                                if (mVendorInfoApi.getVendor().getCuisine() != null) {
                                                    for (int i = 0; i < mVendorInfoApi.getVendor().getCuisine().size(); i++) {
                                                        if (mCuisine.toString().isEmpty()) {
                                                            mCuisine.append(mVendorInfoApi.getVendor().getCuisine().get(i).getName());
                                                        } else {
                                                            mCuisine.append(", ").append(mVendorInfoApi.getVendor().getCuisine().get(i).getName());
                                                        }
                                                    }
                                                }
                                                if (!mVendorInfoApi.getVendor().getRating().getRating().isEmpty()) {
                                                    binding.ratingCount.setText(mVendorInfoApi.getVendor().getRating().getRating());
                                                    binding.reviewText.setText(mVendorInfoApi.getVendor().getRating().getName());
                                                } else {
                                                    binding.reviewText.setText("No Rating");
                                                    binding.ratingCount.setText(mVendorInfoApi.getVendor().getRating().getRating());
                                                }
                                                binding.resCuisines.setText(mCuisine.toString());
                                                binding.resArea.setText(mVendorInfoApi.getVendor().getAddress());
                                                binding.resDeliveryFee.setText(mVendorInfoApi.getVendor().getDeliveryCharge());
                                                binding.resDeliveryTime.setText(mVendorInfoApi.getVendor().getDeliveryTime());
                                                binding.resMinOrder.setText(mVendorInfoApi.getVendor().getMinimumAmount());
                                                binding.resOpeningHours.setText(mVendorInfoApi.getVendor().getWorking_hours());
                                                binding.paymentRecView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
                                                binding.paymentRecView.setAdapter(new PaymentListAdapter(mVendorInfoApi.getVendor().getPaymentMethod()));*/

                                        } else {
                                            //Api response failure :-
                                            if (getActivity() != null) {
                                                if (mGroceryInfoApi.error != null) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mGroceryInfoApi.error.message);
                                                }
                                            }
                                        }


                                    }
                                } else {
                                    mProgressDialog.cancel();
                                    String mErrorMsgToShow = "";
                                    try {
                                        ResponseBody requestBody = response.errorBody();
                                        if (requestBody != null) {
                                            mErrorMsgToShow = AppFunctions.apiResponseErrorMsg(getActivity(), requestBody);
                                        } else {
                                            mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
                                        }
                                    } catch (Exception e) {
                                        // e.printStackTrace();
                                        mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
                                    }
                                    AppFunctions.msgDialogOk(getActivity(), "", mErrorMsgToShow);
                                }

                            }


                        }

                        @Override
                        public void onFailure(@NonNull Call<GroceryInfoApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });
                } catch (JSONException e) {
                    mProgressDialog.cancel();
                    e.printStackTrace();
                }
            } else {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_home_restaurant_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }


        }


    }


}