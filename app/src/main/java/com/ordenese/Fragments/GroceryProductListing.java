package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
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
import com.ordenese.DataSets.GPLApiResponseCheck;
import com.ordenese.DataSets.GroceryCategoryDataSet;
import com.ordenese.DataSets.GroceryProducts;
import com.ordenese.DataSets.GroceryProductsList;
import com.ordenese.DataSets.SubCategoryDataSet;
import com.ordenese.DataSets.SubCategoryList;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.MakeBottomMarginForViewBasket;
import com.ordenese.Interfaces.OnLoadMoreListener;
import com.ordenese.R;
import com.ordenese.databinding.FragmentGroceryProductListingBinding;
import com.ordenese.databinding.GroceryCmpBottomBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroceryProductListing extends Fragment {

    FragmentGroceryProductListingBinding binding;
    String category_id = "", vendor_id = "", vendor_name = "", vendor_status = "";
    int category_position = 0;
    Activity activity;
    ArrayList<GroceryCategoryDataSet> categoryListingArrayList;
    TitleListAdapter adapter;
    SubCategoryAdapter subCategoryAdapter;
    ProductListAdapter productListAdapter;
    RetrofitInterface retrofitInterface;
    ArrayList<GroceryProductsList> groceryProductsListArrayList = new ArrayList<>();
    private ProgressDialog mProgressDialog;
    CartInfo cartInfo;

    private int visibleThreshold = 20;
    private int lastVisibleItem, totalItemCount;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean loading;
    int mPageCountForProduct = 1, total = 0;

    private JSONObject mAddToCartObject;

    private MakeBottomMarginForViewBasket mMakeBottomMarginForViewBasket;

    private CardView mGlobal_CVInitialAddToCart, mGlobal_CVQtyAddToCart;
    private ImageButton mGlobal_CartCountDelete, mGlobal_CartCountRemove;
    private String mCurrentQty = "", mCurrentProductId = "", mCurrentProductName = "", mCurrentVendorLatitude = "", mCurrentVendorLongitude = "",
            mCurrentCategoryId = "", mCurrentSub_category_id = "";

    private int mGroceryProductPosition = -1;

    public GroceryProductListing() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            vendor_id = getArguments().getString("vendor_id");
            vendor_name = getArguments().getString("vendor_name");
            vendor_status = getArguments().getString("vendor_status");

            mCurrentVendorLatitude = getArguments().getString(DefaultNames.vendor_latitude);
            mCurrentVendorLongitude = getArguments().getString(DefaultNames.vendor_longitude);

            category_id = getArguments().getString("category_id");
            category_position = getArguments().getInt("category_position");
            categoryListingArrayList = (ArrayList<GroceryCategoryDataSet>) getArguments().getSerializable("category_list");

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
        activity = getActivity();
        cartInfo = (CartInfo) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGroceryProductListingBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        // binding.nestedView

        mMakeBottomMarginForViewBasket = (MakeBottomMarginForViewBasket) getActivity();

        binding.laySearchTextSearchItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                GroceryAllFeaturedProducts m_groceryAllFeaturedProducts = new GroceryAllFeaturedProducts();
                Bundle bundle = new Bundle();
                bundle.putString(DefaultNames.from, DefaultNames.callFromGroceryCategory);
                bundle.putString(DefaultNames.store_id, vendor_id);
                //bundle.putString(DefaultNames.store_name, vendor_name);
                // bundle.putString(DefaultNames.store_status, vendor_status);
                m_groceryAllFeaturedProducts.setArguments(bundle);
                mFT.replace(R.id.layout_app_home_body, m_groceryAllFeaturedProducts, "m_groceryAllFeaturedProducts");
                mFT.addToBackStack("m_groceryAllFeaturedProducts");
                mFT.commit();

            }
        });


        binding.imageGplCategoryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (categoryListingArrayList != null && categoryListingArrayList.size() > 0) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    CategoriesListBottom categoriesListBottom = new CategoriesListBottom();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("category_list", categoryListingArrayList);
                    bundle.putSerializable("vendor_id", vendor_id);
                    bundle.putSerializable("vendor_name", vendor_name);
                    bundle.putSerializable("vendor_status", vendor_status);
                    categoriesListBottom.setArguments(bundle);
                    categoriesListBottom.show(mFT, "categoriesListBottom");

                }
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        if (!category_id.isEmpty()) {
            if (categoryListingArrayList != null && categoryListingArrayList.size() != 0) {

                binding.categoryRecView.setVisibility(View.VISIBLE);
                binding.subCategoryRecView.setVisibility(View.VISIBLE);
                binding.productListRecView.setVisibility(View.VISIBLE);

                binding.emptyProductList.setVisibility(View.GONE);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false);
                binding.categoryRecView.setLayoutManager(linearLayoutManager);
                adapter = new TitleListAdapter(categoryListingArrayList, activity, category_position, true, linearLayoutManager);
                binding.categoryRecView.setAdapter(adapter);
                binding.categoryRecView.scrollToPosition(category_position);

                get_sub_category(category_id, 0);
            } else {
                binding.categoryRecView.setVisibility(View.GONE);
                binding.subCategoryRecView.setVisibility(View.GONE);
                binding.productListRecView.setVisibility(View.GONE);

                binding.emptyProductList.setVisibility(View.VISIBLE);
                mProgressDialog.cancel();
            }
        } else {
            binding.categoryRecView.setVisibility(View.GONE);
            binding.subCategoryRecView.setVisibility(View.GONE);
            binding.productListRecView.setVisibility(View.GONE);

            binding.emptyProductList.setVisibility(View.VISIBLE);
            mProgressDialog.cancel();
        }


        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        mPageCountForProduct = 1;
        cart_product_count();


    }

    @Override
    public void onStop() {
        super.onStop();
        cartInfo.cart_info(false, "", "");
        mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
    }

    private void cart_product_count() {

        if (AppFunctions.networkAvailabilityCheck(activity)) {

            mProgressDialog.show();

            try {

                retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                String mCustomerAuthorization = "";
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
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

    private void get_sub_category(String categoryId, int position) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.vendor_id, vendor_id);
                    jsonObject.put(DefaultNames.vendor_type_id, "2");
                    jsonObject.put(DefaultNames.category_id, categoryId);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<SubCategoryDataSet> Call = retrofitInterface.subcategory(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<SubCategoryDataSet>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(@NonNull Call<SubCategoryDataSet> call, @NonNull Response<SubCategoryDataSet> response) {
                            if (response.isSuccessful()) {
                                SubCategoryDataSet subCategory = response.body();
                                if (subCategory != null) {
                                    if (subCategory.getSubCategory() != null && subCategory.getSubCategory().size() != 0) {
                                        binding.emptyProductList.setVisibility(View.GONE);
                                        binding.subCategoryRecView.setVisibility(View.VISIBLE);
                                        binding.subCategoryRecView.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
                                        subCategoryAdapter = new SubCategoryAdapter(subCategory.getSubCategory(), activity, position, true, categoryId);
                                        binding.subCategoryRecView.setAdapter(subCategoryAdapter);
                                        subCategoryAdapter.notifyDataSetChanged();
                                        get_product(subCategory.getSubCategory().get(0).getCategoryId(), subCategory.getSubCategory().get(0).getSubCategoryId());
                                    } else {
                                        mProgressDialog.cancel();
                                        binding.subCategoryRecView.setVisibility(View.GONE);
                                        binding.productListRecView.setVisibility(View.GONE);
                                        binding.emptyProductList.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    mProgressDialog.cancel();
                                    binding.subCategoryRecView.setVisibility(View.GONE);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<SubCategoryDataSet> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }

                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();
                    //Log.e("415 Excep ", e.toString());
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

    private void get_product(String categoryId, String sub_category_id) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {

                    mCurrentCategoryId = categoryId;
                    mCurrentSub_category_id = sub_category_id;

                    jsonObject.put(DefaultNames.vendor_id, vendor_id);
                    jsonObject.put(DefaultNames.vendor_type_id, "2");
                    jsonObject.put(DefaultNames.category_id, mCurrentCategoryId);
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    mPageCountForProduct = 1;
                    jsonObject.put(DefaultNames.page, mPageCountForProduct);
                    jsonObject.put(DefaultNames.page_per_unit, DefaultNames.pageCountGrocery);
                    jsonObject.put(DefaultNames.sub_category_id, mCurrentSub_category_id);
                    jsonObject.put(DefaultNames.search, "");
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    //type -1 => only one vendor product are listed.
                    //type - 2 => All vendor product are listed here.
                    jsonObject.put(DefaultNames.type, "1");
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());


                    String mCustomerAuthorization = "";
                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GroceryProducts> Call = retrofitInterface.product(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<GroceryProducts>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(@NonNull Call<GroceryProducts> call, @NonNull Response<GroceryProducts> response) {
                            if (response.isSuccessful()) {
                                GroceryProducts groceryProducts = response.body();
                                binding.categoryRecView.setVisibility(View.VISIBLE);
                                binding.subCategoryRecView.setVisibility(View.VISIBLE);

                                if (groceryProducts != null) {

                                    total = Integer.parseInt(groceryProducts.getTotal());
                                    if (groceryProducts.getProduct().size() != 0) {
                                        groceryProductsListArrayList = groceryProducts.getProduct();
                                        binding.productListRecView.setVisibility(View.VISIBLE);
                                        binding.emptyProductList.setVisibility(View.GONE);

                                        binding.productListRecView.setLayoutManager(new GridLayoutManager(activity, 2, RecyclerView.VERTICAL, false));
                                        productListAdapter = new ProductListAdapter(groceryProductsListArrayList);
                                        binding.productListRecView.setAdapter(productListAdapter);
                                        productListAdapter.notifyDataSetChanged();
                                        setOnLoadMoreListener(new OnLoadMoreListener() {
                                            @Override
                                            public void onLoadMore() {

                                                if (mPageCountForProduct <= ((total / 20) + 1)) {
                                                    mPageCountForProduct++;

                                                    if (getActivity() != null) {
                                                        if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                                                            JSONObject jsonObject = new JSONObject();

                                                            //**************************************
                                                            //**************************************
                                                            //**************************************

                                                            try {

                                                                jsonObject.put(DefaultNames.vendor_id, vendor_id);
                                                                jsonObject.put(DefaultNames.vendor_type_id, "2");
                                                                jsonObject.put(DefaultNames.category_id, mCurrentCategoryId);
                                                                jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                                                                jsonObject.put(DefaultNames.page, mPageCountForProduct);
                                                                jsonObject.put(DefaultNames.page_per_unit, DefaultNames.pageCountGrocery);
                                                                jsonObject.put(DefaultNames.sub_category_id, mCurrentSub_category_id);
                                                                jsonObject.put(DefaultNames.search, "");
                                                                if (AppFunctions.isUserLoggedIn(getActivity())) {
                                                                    jsonObject.put(DefaultNames.guest_status, "0");
                                                                    jsonObject.put(DefaultNames.guest_id, "");
                                                                } else {
                                                                    jsonObject.put(DefaultNames.guest_status, "1");
                                                                    jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                                                                }
                                                                //type -1 => only one vendor product are listed.
                                                                //type - 2 => All vendor product are listed here.
                                                                jsonObject.put(DefaultNames.type, "1");
                                                                jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                                                                jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                                                                jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                                                                RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());


                                                                String mCustomerAuthorization = "";
                                                                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                                                    mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                                                                }

                                                                retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                                                                Call<GroceryProducts> Call = retrofitInterface.product(mCustomerAuthorization, body);
                                                                mProgressDialog.show();
                                                                Call.enqueue(new Callback<GroceryProducts>() {
                                                                    @SuppressLint("NotifyDataSetChanged")
                                                                    @Override
                                                                    public void onResponse(@NonNull Call<GroceryProducts> call, @NonNull Response<GroceryProducts> response) {
                                                                        if (response.isSuccessful()) {
                                                                            GroceryProducts groceryProducts = response.body();
                                                                            if (groceryProducts != null) {
                                                                                if (groceryProducts.getProduct().size() != 0) {

                                                                                    groceryProductsListArrayList.addAll(groceryProducts.getProduct());
                                                                                    productListAdapter.notifyDataSetChanged();
                                                                                    setLoaded();

                                                                                } else {

                                                                                }

                                                                            } else {

                                                                            }


                                                                        }

                                                                        mProgressDialog.cancel();

                                                                    }

                                                                    @Override
                                                                    public void onFailure(@NonNull Call<GroceryProducts> call, @NonNull Throwable t) {
                                                                        mProgressDialog.cancel();
                                                                    }

                                                                });


                                                            } catch (JSONException e) {
                                                                mProgressDialog.cancel();
                                                                //Log.e("415 Excep ", e.toString());
                                                                e.printStackTrace();
                                                            }

                                                            //**************************************
                                                            //**************************************
                                                            //**************************************

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
                                        });

                                        binding.nestedView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                            @Override
                                            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                                if (v.getChildAt(v.getChildCount() - 1) != null) {
                                                    if (scrollY > oldScrollY) {
                                                        if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                                                            //code to fetch more data for endless scrolling
                                                            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.productListRecView.getLayoutManager();
                                                            assert linearLayoutManager != null;
                                                            totalItemCount = linearLayoutManager.getItemCount();
                                                            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                                                            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                                                if (onLoadMoreListener != null) {
                                                                    onLoadMoreListener.onLoadMore();
                                                                }
                                                                loading = true;
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        });


                                    } else {
                                        binding.emptyProductList.setVisibility(View.VISIBLE);
                                        binding.productListRecView.setVisibility(View.GONE);
                                    }

                                } else {

                                    binding.emptyProductList.setVisibility(View.VISIBLE);
                                    binding.productListRecView.setVisibility(View.GONE);

                                }
                            }
                            mProgressDialog.cancel();
                        }

                        @Override
                        public void onFailure(@NonNull Call<GroceryProducts> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }

                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();
                    //Log.e("415 Excep ", e.toString());
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

    class TitleListAdapter extends RecyclerView.Adapter<TitleListAdapter.DataObjectHolder> {

        private final ArrayList<GroceryCategoryDataSet> mTitleList;
        private TextView mTempMenuTitleName;
        private Boolean mIsInitial = true;
        private final int mPositionToMove;
        private Boolean mIsFromItemTouch;
        Activity activity;
        LinearLayoutManager linearLayoutManager;

        public TitleListAdapter(ArrayList<GroceryCategoryDataSet> titleList, Activity activity, int positionToMove, Boolean isFromItemTouch,
                                LinearLayoutManager linearLayoutManager) {
            this.mTitleList = titleList;
            this.activity = activity;
            this.mPositionToMove = positionToMove;
            this.mIsFromItemTouch = isFromItemTouch;
            this.linearLayoutManager = linearLayoutManager;
        }

        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grocery_menu_title_list_row, parent, false);
            return new DataObjectHolder(mView);
        }


        @Override
        public void onBindViewHolder(final DataObjectHolder holder, final int position) {

            holder.mMenuTitleName.setText(mTitleList.get(position).name);

//            Log.e("onBindViewHolder: ", mPositionToMove + " /" + position);

            if (mIsFromItemTouch) {
                if (mPositionToMove == position) {
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_category_selected));
                    }
                    mTempMenuTitleName = holder.mMenuTitleName;
                } else {
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                        holder.mMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                    }
                }
            } else {
                if (mIsInitial) {
                    //safe check :-
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_category_selected));
                    }
                    mTempMenuTitleName = holder.mMenuTitleName;
                    mIsInitial = false;
                } else {
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                        holder.mMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                    }
                }
            }

            holder.mRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsFromItemTouch = false;
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_category_selected));
                    }
                    mTempMenuTitleName = holder.mMenuTitleName;

                    category_id = mTitleList.get(position).category_id;
                    get_sub_category(mTitleList.get(position).category_id, 0);
                }
            });

            if (position == mTitleList.size() - 1) {
                holder.mTitleView.setVisibility(View.GONE);
            } else {
                holder.mTitleView.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return mTitleList.size();
        }

        class DataObjectHolder extends RecyclerView.ViewHolder {

            private final TextView mMenuTitleName;
            //            private final View mBarView;
            private final View mTitleView;
            private final LinearLayout mRow;

            public DataObjectHolder(View view) {
                super(view);
                mMenuTitleName = view.findViewById(R.id.tv_store_menu_title);
//                mBarView = view.findViewById(R.id.view_store_menu_title_bar);
                mTitleView = view.findViewById(R.id.view_store_menu_title);
                mRow = view.findViewById(R.id.lay_store_menu_title_list_row);
            }
        }

    }

    class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.DataObjectHolder> {

        private final ArrayList<SubCategoryList> mTitleList;
        private TextView mTempMenuTitleName;
        private Boolean mIsInitial = true;
        private final int mPositionToMove;
        private Boolean mIsFromItemTouch;
        Activity activity;
        String category_id = "";

        public SubCategoryAdapter(ArrayList<SubCategoryList> titleList, Activity activity, int positionToMove, Boolean isFromItemTouch, String category_id) {
            this.mTitleList = titleList;
            this.activity = activity;
            this.mPositionToMove = positionToMove;
            this.mIsFromItemTouch = isFromItemTouch;
            this.category_id = category_id;
        }

        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grocery_menu_title_list_row, parent, false);
            return new DataObjectHolder(mView);
        }

        @Override
        public void onBindViewHolder(final DataObjectHolder holder, final int position) {

            holder.mMenuTitleName.setText(mTitleList.get(position).getName());

//            //Log.e("onBindViewHolder: ", mPositionToMove + " /" + position);

            if (mIsFromItemTouch) {
                if (mPositionToMove == position) {
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_sub_category_un_select));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_sub_category));
                    }
                    mTempMenuTitleName = holder.mMenuTitleName;
                } else {
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_sub_category_un_select));
                    }
                }
            } else {
                if (mIsInitial) {
                    //safe check :-
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_sub_category_un_select));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_sub_category));
                    }
                    mTempMenuTitleName = holder.mMenuTitleName;
                    mIsInitial = false;
                } else {
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_sub_category_un_select));
                    }
                }
            }

            holder.mRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsFromItemTouch = false;
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_sub_category_un_select));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_sub_category));
                    }
                    mTempMenuTitleName = holder.mMenuTitleName;

                    get_product(category_id, mTitleList.get(position).getSubCategoryId());
                }
            });

            if (position == mTitleList.size() - 1) {
                holder.mTitleView.setVisibility(View.GONE);
            } else {
                holder.mTitleView.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return mTitleList.size();
        }

        class DataObjectHolder extends RecyclerView.ViewHolder {

            private final TextView mMenuTitleName;
            //            private final View mBarView;
            private final View mTitleView;
            private final LinearLayout mRow;

            public DataObjectHolder(View view) {
                super(view);
                mMenuTitleName = view.findViewById(R.id.tv_store_menu_title);
//                mBarView = view.findViewById(R.id.view_store_menu_title_bar);
                mTitleView = view.findViewById(R.id.view_store_menu_title);
                mRow = view.findViewById(R.id.lay_store_menu_title_list_row);
            }
        }

    }

    class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

        ArrayList<GroceryProductsList> mProductList;

        public ProductListAdapter(ArrayList<GroceryProductsList> category_List) {
            this.mProductList = category_List;
        }

        public void toUpdateProductsList(ArrayList<GroceryProductsList> productList) {
            this.mProductList = productList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.grocery_category_product_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //  ////Log.e("CartProductListAdapter onBindViewHolder","");

            holder.mCategoryName.setText(mProductList.get(position).getItemName());

            if (!mProductList.get(position).getDiscount().isEmpty()) {
                holder.mPrice.setVisibility(View.VISIBLE);
                holder.mSpecialPrice.setText(mProductList.get(position).getDiscount());
                holder.mPrice.setText(mProductList.get(position).getPrice());
                holder.mPrice.setPaintFlags(holder.mPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.mSpecialPrice.setText(mProductList.get(position).getPrice());
                holder.mPrice.setVisibility(View.GONE);
            }

            if (getActivity() != null) {
                Glide.with(getActivity()).load(mProductList.get(position).getLogo()).into(holder.mCategoryImg);
            }

            String mCartQTY = mProductList.get(position).getCart_qty();
            if (mCartQTY != null && !mCartQTY.isEmpty()) {
                holder.mCVInitialAddToCart.setVisibility(View.GONE);
                holder.mCVQtyAddToCart.setVisibility(View.VISIBLE);
                if (Integer.parseInt(mCartQTY) > 1) {
                    holder.mCartCountDelete.setVisibility(View.GONE);
                    holder.mCartCountRemove.setVisibility(View.VISIBLE);
                } else {
                    holder.mCartCountDelete.setVisibility(View.VISIBLE);
                    holder.mCartCountRemove.setVisibility(View.GONE);
                }
                holder.mCartCount.setText(mCartQTY);

            } else {
                holder.mCVInitialAddToCart.setVisibility(View.VISIBLE);
                holder.mCVQtyAddToCart.setVisibility(View.GONE);

            }

            // holder.mCartCountAdd.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.baseline_add_primary_color_18dp));
            holder.mCVInitialAddToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // holder.mCVInitialAddToCart.setVisibility(View.GONE);
                    // holder.mCVQtyAddToCart.setVisibility(View.VISIBLE);

                    mGlobal_CVInitialAddToCart = holder.mCVInitialAddToCart;
                    mGlobal_CVQtyAddToCart = holder.mCVQtyAddToCart;
                    mGlobal_CartCountDelete = holder.mCartCountDelete;
                    mGlobal_CartCountRemove = holder.mCartCountRemove;

                    mCurrentQty = "1";
                    mCurrentProductId = mProductList.get(position).getProductItemId();
                    mCurrentProductName = mProductList.get(position).getItemName();

                    if (vendor_status != null && vendor_status.equals("0")) {
                        //Currently vendor closed so to show message to user
                        //and perform add to cart :-
                        mGroceryProductPosition = position;
                        toPerformAddToCart();
                    } else {
                        //here restaurant is open or busy :-
                        mGroceryProductPosition = position;
                        add_to_cart();
                    }


                }
            });

            holder.mCartCountAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String productCartId = mProductList.get(position).getCart_id();

                    String t = holder.mCartCount.getText().toString();

                    int mCount = Integer.valueOf(t);
                    mCount++;

                    //type = 0 (decrement) | type = 1 (increment) :-
                    mGroceryProductPosition = position;
                    callCartItemIncrementOrDecrementAPi(productCartId, "1", holder.mCartCount, String.valueOf(mCount)
                            , holder.mCartCountDelete, holder.mCartCountRemove);


                }
            });

            holder.mCartCountDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String productCartId = mProductList.get(position).getCart_id();

                    if (getActivity() != null) {
                        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
                        alertDialogBuilder
                                .setMessage(getActivity().getString(R.string.do_you_want_remove_this_item))
                                .setCancelable(true)
                                .setPositiveButton(getActivity().getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        mGroceryProductPosition = position;
                                        callCartItemDeleteAPi(productCartId, holder.mCVInitialAddToCart, holder.mCVQtyAddToCart);

                                        dialog.dismiss();
                                    }
                                }).setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                }
                        );

                        androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }


                }
            });

            holder.mCartCountRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String productCartId = mProductList.get(position).getCart_id();

                    String t = holder.mCartCount.getText().toString();
                    int mCount = Integer.valueOf(t);
                    mCount--;

                    //type = 0 (decrement) | type = 1 (increment) :-
                    mGroceryProductPosition = position;
                    callCartItemIncrementOrDecrementAPi(productCartId, "0", holder.mCartCount, String.valueOf(mCount)
                            , holder.mCartCountDelete, holder.mCartCountRemove);

                }
            });

            holder.mLayRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    AppFunctions.toastShort(getActivity(), mProductList.get(position).getItemName());

                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    GroceryProductDetails groceryProductDetails = new GroceryProductDetails();
                    Bundle mBundle = new Bundle();
                    mBundle.putString("vendor_id", vendor_id);
                    mBundle.putString("vendor_name", vendor_name);
                    mBundle.putString("vendor_status", vendor_status);
                    mBundle.putString("product_id", mProductList.get(position).getProductItemId());
                    mBundle.putString("latitude", AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode().getmLatitude());
                    mBundle.putString("longitude", AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode().getmLongitude());
                    groceryProductDetails.setArguments(mBundle);
                    mFT.replace(R.id.layout_app_home_body, groceryProductDetails, "groceryProductDetails");
                    mFT.addToBackStack("groceryProductDetails");
                    mFT.commit();

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
            return mProductList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView mCategoryName, mSpecialPrice, mPrice;
            private ImageView mCategoryImg;
            private LinearLayout mLayRow;
            private CardView mCVInitialAddToCart, mCVQtyAddToCart;

            private ImageButton mCartCountAdd, mCartCountRemove, mCartCountDelete;
            private TextView mCartCount;

            public ViewHolder(View itemView) {
                super(itemView);

                mSpecialPrice = itemView.findViewById(R.id.tv_gc_l_pl_special_price);
                mPrice = itemView.findViewById(R.id.tv_gc_l_pl_price);

                mCategoryName = itemView.findViewById(R.id.tv_gc_l_pl_name);
                mCategoryImg = itemView.findViewById(R.id.iv_gc_l_pl_image);
                mLayRow = itemView.findViewById(R.id.lay_gc_list_pl_row);
                mCVInitialAddToCart = itemView.findViewById(R.id.card_view_initial_cart_add);
                mCVQtyAddToCart = itemView.findViewById(R.id.card_view_cart_add_with_qty);
                mCartCountAdd = itemView.findViewById(R.id.img_cpl_add);
                mCartCountRemove = itemView.findViewById(R.id.img_cpl_remove);
                mCartCount = itemView.findViewById(R.id.tv_cpl_item_count);
                mCartCountDelete = itemView.findViewById(R.id.img_cpl_delete);


            }

            @Override
            public void onClick(View v) {

            }
        }
    }

    void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    void setLoaded() {
        loading = false;
    }

    public static class CategoriesListBottom extends BottomSheetDialogFragment {

        GroceryCmpBottomBinding binding_;
        ArrayList<GroceryCategoryDataSet> categoryListingArrayList;
        String vendor_id = "", vendor_name = "", vendor_status = "";

        public CategoriesListBottom() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
            if (getArguments() != null) {
                categoryListingArrayList = (ArrayList<GroceryCategoryDataSet>) getArguments().getSerializable("category_list");
                vendor_id = getArguments().getString("vendor_id");
                vendor_name = getArguments().getString("vendor_name");
                vendor_status = getArguments().getString("vendor_status");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            binding_ = GroceryCmpBottomBinding.inflate(inflater, container, false);


            binding_.recyclerGCmpBList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
            binding_.recyclerGCmpBList.setAdapter(new AllCategoryListAdapter(categoryListingArrayList));
            binding_.imgGCmpBCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getDialog() != null) {
                        getDialog().dismiss();
                    }
                }
            });


            return binding_.getRoot();
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

                holder.mCategoryName.setText(m_Category_List.get(position).name);

                if (getActivity() != null) {
                    holder.mCategoryImg.setVisibility(View.VISIBLE);
                    holder.mCategoryImgMoreThan11.setVisibility(View.GONE);
                    Glide.with(getActivity()).load(m_Category_List.get(position).picture).into(holder.mCategoryImg);
                }

                holder.mLayRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toPerformCategoryProductCall(position);
                    }
                });
            }

            private void toPerformCategoryProductCall(int currentPosition) {

                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                GroceryProductListing groceryProductListing = new GroceryProductListing();
                Bundle mBundle = new Bundle();
                mBundle.putString("category_id", m_Category_List.get(currentPosition).category_id);
                mBundle.putString("vendor_id", vendor_id);
                mBundle.putString("vendor_name", vendor_name);
                mBundle.putString("vendor_status", vendor_status);

                mBundle.putString(DefaultNames.vendor_latitude, groceryProductListing.mCurrentVendorLatitude);
                mBundle.putString(DefaultNames.vendor_longitude, groceryProductListing.mCurrentVendorLongitude);

                mBundle.putInt("category_position", currentPosition);
                mBundle.putSerializable("category_list", m_Category_List);
                groceryProductListing.setArguments(mBundle);
                mFT.replace(R.id.layout_app_home_body, groceryProductListing, "groceryProductListing");
                mFT.addToBackStack("groceryProductListing");
                mFT.commit();

                if (getDialog() != null) {
                    getDialog().dismiss();
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


    }

    private void add_to_cart() {


        if (AppFunctions.networkAvailabilityCheck(activity)) {

            mAddToCartObject = new JSONObject();
            JSONObject product_obj = new JSONObject();
            JSONArray product_array = new JSONArray();
            JSONObject option_object = new JSONObject();
            try {
                product_obj.put(DefaultNames.product_id, mCurrentProductId);
                product_obj.put(DefaultNames.quantity, mCurrentQty);
                product_obj.put(DefaultNames.option, option_object);
                product_array.put(product_obj);
                mAddToCartObject.put(DefaultNames.products, product_array);
                mAddToCartObject.put(DefaultNames.latitude, "0.0");
                mAddToCartObject.put(DefaultNames.longitude, "0.0");
                mAddToCartObject.put(DefaultNames.vendor_id, vendor_id);
                if (AppFunctions.isUserLoggedIn(getActivity())) {
                    mAddToCartObject.put(DefaultNames.guest_status, "0");
                    mAddToCartObject.put(DefaultNames.guest_id, "");
                } else {
                    mAddToCartObject.put(DefaultNames.guest_status, "1");
                    mAddToCartObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                }
                mAddToCartObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                mAddToCartObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                mAddToCartObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                mAddToCartObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                RequestBody body = RequestBody.create(MediaType.parse("application/json"), mAddToCartObject.toString());


                mProgressDialog.show();

                retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                String mCustomerAuthorization = "";
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                }
                Call<String> call = retrofitInterface.add_to_cart(mCustomerAuthorization, body);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        mAddToCartObject = null;

                        if (response.isSuccessful()) {
                            try {
                                JSONObject obj = new JSONObject(response.body());
                                if (!obj.isNull("error")) {
                                    JSONObject jsonObject = obj.getJSONObject("error");
                                    if (!jsonObject.isNull("status") && jsonObject.getString("status").equals("007")) {
                                        if (!obj.isNull("error_warning")) {
                                            if (!obj.getString("error_warning").isEmpty()) {
                                                delete_cart_dialog(obj.getString("error_warning"));
                                            }
                                        }
                                    } else {
                                        if (!obj.isNull("error_warning")) {
                                            if (!obj.getString("error_warning").isEmpty()) {
                                                getDialog(obj.getString("error_warning"));
                                            }
                                        }
                                    }
                                }
                                if (!obj.isNull("success")) {
                                    JSONObject jsonObject = obj.getJSONObject("success");
                                    if (!jsonObject.isNull("message")) {
                                        if (!jsonObject.getString("message").isEmpty()) {

                                        }
                                    }
                                    if (!obj.isNull("error_warning")) {
                                        if (!obj.getString("error_warning").isEmpty()) {
                                            getDialog(obj.getString("error_warning"));
                                        } else {

                                            cart_product_count();
                                            GroceryProductsList mGroceryPDS = new GroceryProductsList();
                                            if (!obj.isNull("product_info")) {
                                                JSONObject objectProductInfo = obj.getJSONObject("product_info");
                                                if (!objectProductInfo.isNull("product_item_id") && !objectProductInfo.getString("product_item_id").isEmpty()) {
                                                    mGroceryPDS.setProductItemId(objectProductInfo.getString("product_item_id"));
                                                } else {
                                                    mGroceryPDS.setProductItemId("");
                                                }
                                                if (!objectProductInfo.isNull("item_name") && !objectProductInfo.getString("item_name").isEmpty()) {
                                                    mGroceryPDS.setItemName(objectProductInfo.getString("item_name"));
                                                } else {
                                                    mGroceryPDS.setItemName("");
                                                }
                                                if (!objectProductInfo.isNull("description") && !objectProductInfo.getString("description").isEmpty()) {
                                                    mGroceryPDS.setDescription(objectProductInfo.getString("description"));
                                                } else {
                                                    mGroceryPDS.setDescription("");
                                                }
                                                if (!objectProductInfo.isNull("price") && !objectProductInfo.getString("price").isEmpty()) {
                                                    mGroceryPDS.setPrice(objectProductInfo.getString("price"));
                                                } else {
                                                    mGroceryPDS.setPrice("");
                                                }
                                                if (!objectProductInfo.isNull("qty") && !objectProductInfo.getString("qty").isEmpty()) {
                                                    mGroceryPDS.setQty(objectProductInfo.getString("qty"));
                                                } else {
                                                    mGroceryPDS.setQty("");
                                                }
                                                if (!objectProductInfo.isNull("cart_id") && !objectProductInfo.getString("cart_id").isEmpty()) {
                                                    mGroceryPDS.setCart_id(objectProductInfo.getString("cart_id"));
                                                } else {
                                                    mGroceryPDS.setCart_id("");
                                                }
                                                if (!objectProductInfo.isNull("cart_qty") && !objectProductInfo.getString("cart_qty").isEmpty()) {
                                                    mGroceryPDS.setCart_qty(objectProductInfo.getString("cart_qty"));
                                                } else {
                                                    mGroceryPDS.setCart_qty("");
                                                }
                                                if (!objectProductInfo.isNull("discount") && !objectProductInfo.getString("discount").isEmpty()) {
                                                    mGroceryPDS.setDiscount(objectProductInfo.getString("discount"));
                                                } else {
                                                    mGroceryPDS.setDiscount("");
                                                }
                                                if (!objectProductInfo.isNull("logo") && !objectProductInfo.getString("logo").isEmpty()) {
                                                    mGroceryPDS.setLogo(objectProductInfo.getString("logo"));
                                                } else {
                                                    mGroceryPDS.setLogo("");
                                                }
                                                if (!objectProductInfo.isNull("picture") && !objectProductInfo.getString("picture").isEmpty()) {
                                                    mGroceryPDS.setPicture(objectProductInfo.getString("picture"));
                                                } else {
                                                    mGroceryPDS.setPicture("");
                                                }

                                                if (mGroceryProductPosition != -1
                                                        && groceryProductsListArrayList != null
                                                        && groceryProductsListArrayList.size() > 0
                                                        && productListAdapter != null) {

                                                    groceryProductsListArrayList.set(mGroceryProductPosition, mGroceryPDS);
                                                    productListAdapter.notifyDataSetChanged();
                                                    setLoaded();
                                                }
                                            }


                                        }
                                    }


                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        mProgressDialog.cancel();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        mProgressDialog.cancel();
                        mAddToCartObject = null;

                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
                mProgressDialog.cancel();

            }


        } else {
            mProgressDialog.cancel();

            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
            mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
            mFT.addToBackStack("mNetworkAnalyser");
            mFT.commit();
        }


    }

    private void getDialog(String data) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(data);
        builder.setCancelable(true);
        builder.setPositiveButton(activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });
        builder.create();
        builder.show();


    }

    private void delete_cart_dialog(String data) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(data);
        builder.setTitle(activity.getResources().getString(R.string.start_a_new_basket));
        builder.setPositiveButton(activity.getResources().getString(R.string.agree), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete_cart();
            }
        });
        builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    private void delete_cart() {
        mProgressDialog.show();
        if (AppFunctions.networkAvailabilityCheck(activity)) {
            JSONObject object = new JSONObject();
            try {
                object.put(DefaultNames.product_cart_id, "");
                object.put(DefaultNames.clear, "1");
                if (AppFunctions.isUserLoggedIn(getActivity())) {
                    object.put(DefaultNames.guest_status, "0");
                    object.put(DefaultNames.guest_id, "");
                } else {
                    object.put(DefaultNames.guest_status, "1");
                    object.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                }
                object.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                object.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                object.put(DefaultNames.day_id, AppFunctions.getDayId());
                object.put(DefaultNames.vendor_id, vendor_id);

                RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());
                retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                String mCustomerAuthorization = "";
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                }

                Call<String> call = retrofitInterface.clear_cart(mCustomerAuthorization, body);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            add_to_cart();
//                            AppFunctions.toastShort(activity,response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        mProgressDialog.cancel();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                mProgressDialog.cancel();
            }
        } else {
            mProgressDialog.cancel();
            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
            mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
            mFT.addToBackStack("mNetworkAnalyser");
            mFT.commit();
        }

    }


    private void callCartItemIncrementOrDecrementAPi(String productCartId, String operationType, TextView tvQuantity,
                                                     String count, ImageButton cartCountDelete, ImageButton cartCountRemove) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.cart_id, productCartId);
                    //type = 0 (decrement) | type = 1 (increment) :-
                    jsonObject.put(DefaultNames.type, operationType);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    String mCustomerAuthorization = "";
                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.vendor_id, vendor_id);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GPLApiResponseCheck> Call = retrofitInterface.groceryPLCartItemIncrementOrDecrementApi(mCustomerAuthorization, body);
                    mProgressDialog.show();

                    Call.enqueue(new Callback<GPLApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<GPLApiResponseCheck> call, @NonNull Response<GPLApiResponseCheck> response) {


                            if (response.isSuccessful()) {

                                GPLApiResponseCheck mGplApiResponseCheck = response.body();
                                if (mGplApiResponseCheck != null) {

                                    if (mGplApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            if (tvQuantity != null) {
                                                tvQuantity.setText(count);


                                                if (operationType.equals("0")) {
                                                    // type = 0 (decrement) :-
                                                    if (Integer.parseInt(count) == 1) {
                                                        cartCountDelete.setVisibility(View.VISIBLE);
                                                        cartCountRemove.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    //type = 1 (increment) :-
                                                    if (Integer.parseInt(count) > 1) {
                                                        cartCountDelete.setVisibility(View.GONE);
                                                        cartCountRemove.setVisibility(View.VISIBLE);
                                                    }
                                                }

                                                cart_product_count();

                                                if (mGroceryProductPosition != -1
                                                        && groceryProductsListArrayList != null
                                                        && groceryProductsListArrayList.size() > 0
                                                        && productListAdapter != null) {

                                                    groceryProductsListArrayList.set(mGroceryProductPosition, mGplApiResponseCheck.GroceryProduct);
                                                    productListAdapter.notifyDataSetChanged();
                                                    setLoaded();

                                                }


                                            }

                                            mProgressDialog.cancel();
                                        }
                                    } else {
                                        //Api response failure :-
                                        mProgressDialog.cancel();
                                        if (getActivity() != null) {
                                            if (mGplApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mGplApiResponseCheck.error.message);
                                            }
                                        }
                                    }

                                } else {
                                    mProgressDialog.cancel();
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

                        @Override
                        public void onFailure(@NonNull Call<GPLApiResponseCheck> call, @NonNull Throwable t) {

                            mProgressDialog.cancel();

                        }
                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();

                    //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();

                }


            } else {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }
    }


    private void callCartItemDeleteAPi(String productCartId, CardView cVInitialAddToCart, CardView cVQtyAddToCart) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {


                JSONObject jsonObject = new JSONObject();
                try {

                    JSONArray productCIdJsonArray = new JSONArray();

                    productCIdJsonArray.put(0, productCartId);

                    jsonObject.put(DefaultNames.product_cart_id, productCIdJsonArray);
                    //Where clear = 0 for delete the requested items from cart list.
                    // If clear = 1 then its delete the all items from cart list:-
                    jsonObject.put(DefaultNames.clear, "0");
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    String mCustomerAuthorization = "";
                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.vendor_id, vendor_id);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GPLApiResponseCheck> Call = retrofitInterface.groceryPLCartItemDeleteApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<GPLApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<GPLApiResponseCheck> call, @NonNull Response<GPLApiResponseCheck> response) {

                            if (response.isSuccessful()) {
                                GPLApiResponseCheck mGplApiResponseCheck = response.body();

                                if (mGplApiResponseCheck != null) {
                                    if (mGplApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            cVInitialAddToCart.setVisibility(View.VISIBLE);
                                            cVQtyAddToCart.setVisibility(View.GONE);
                                            cart_product_count();

                                            if (mGroceryProductPosition != -1
                                                    && groceryProductsListArrayList != null
                                                    && groceryProductsListArrayList.size() > 0
                                                    && productListAdapter != null) {

                                                groceryProductsListArrayList.set(mGroceryProductPosition, mGplApiResponseCheck.GroceryProduct);
                                                productListAdapter.notifyDataSetChanged();
                                                setLoaded();
                                            }
                                            mProgressDialog.cancel();
                                        }
                                    } else {
                                        //Api response failure :-
                                        mProgressDialog.cancel();
                                        if (getActivity() != null) {
                                            if (mGplApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mGplApiResponseCheck.error.message);
                                            }
                                        }
                                    }

                                } else {
                                    mProgressDialog.cancel();
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

                        @Override
                        public void onFailure(@NonNull Call<GPLApiResponseCheck> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();

                    //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();

                }


            } else {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }


    }

    private void toPerformAddToCart() {

        if (getActivity() != null) {
            String mFINALMsg = vendor_name + " " + getActivity().getResources().getString(R.string.pd_is_not_available) + " " +
                    mCurrentProductName + " " + getActivity().getResources().getString(R.string.pd_at_this_time);

            // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            alertDialogBuilder
                    .setMessage(mFINALMsg)
                    .setCancelable(false)
                    .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            add_to_cart();
                            dialog.dismiss();
                        }
                    }).setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }

    }

}