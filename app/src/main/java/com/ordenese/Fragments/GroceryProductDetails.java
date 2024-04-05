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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.GroceryProductDataSet;
import com.ordenese.DataSets.GroceryProductInfo;
import com.ordenese.DataSets.UserProductOptionDataSet;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.TempOptionDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.R;
import com.ordenese.databinding.FragmentGroceryProductDetailsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroceryProductDetails extends Fragment implements View.OnClickListener {

    FragmentGroceryProductDetailsBinding binding;
    RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private String mProductId = "", restaurantId = "", restaurantName = "", restaurantStatus = "", restaurantLatitude = "",
            restaurantLongitude = "", mProductItemPrice = "", mProductPriceDiscount = "",
            mProductName = "", mProductImage = "", mProductDescription = "", mProductMinQty = "", mCurrentQty = "1";
    CartInfo cartInfo;
    Activity activity;
    private JSONObject mAddToCartObject;
    private Boolean mIsProductWithOption = false;

    public GroceryProductDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            restaurantId = getArguments().getString("vendor_id");
            restaurantName = getArguments().getString("vendor_name");
            restaurantStatus = getArguments().getString("vendor_status");
            restaurantLatitude = getArguments().getString("latitude");
            restaurantLongitude = getArguments().getString("longitude");
            mProductId = getArguments().getString("product_id");

            mProductMinQty = "1"; // by default set as 1

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
        binding = FragmentGroceryProductDetailsBinding.inflate(inflater, container, false);

        activity = getActivity();
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();


        //To delete the product options form TempOptionDB  :-
        TempOptionDB.getInstance(getActivity()).deleteProductOptionDB();

        binding.layRestaurantMenuOptionDialogClosedMsg.setVisibility(View.GONE);
        binding.layRestaurantMenuOptionDialogClosedMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.layRestaurantMenuOptionDialogClosedMsg.setVisibility(View.GONE);
            }
        });

        binding.btnRestaurantMenuOptionDialogClosedOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.layRestaurantMenuOptionDialogClosedMsg.setVisibility(View.GONE);
            }
        });

//        layout_restaurant_menu_option_dialog_close

        binding.layoutRestaurantMenuOptionDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.progressBarRestaurantMenuOptionDialog.setVisibility(View.GONE);
        binding.btnRestaurantMenuOptionDialogAddToCart.setOnClickListener(this);

        if (getActivity() != null) {
            binding.btnRestaurantMenuOptionDialogAddToCart.setEnabled(true);
            binding.btnRestaurantMenuOptionDialogAddToCart.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_square_primary_color));
            binding.btnRestaurantMenuOptionDialogAddToCart.setTextColor(getResources().getColor(R.color.white));
            // BtnCartGotoCheckout.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_success_yellow_border_round_btn));
            // mAddToCart.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_btn_disable_status));
        }

        if (getActivity() != null) {
            String mInitialString = getActivity().getString(R.string.add) + " " + getActivity().getString(R.string.to_cart);
            binding.btnRestaurantMenuOptionDialogAddToCart.setText(mInitialString);
        }

        mProgressDialog.cancel();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        get_product_info();

        cartInfo.cart_info(false, "", "");
        mProgressDialog.cancel();


    }

    private void get_product_info() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.vendor_id, restaurantId);
                    jsonObject.put(DefaultNames.product_id, mProductId);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GroceryProductInfo> Call = retrofitInterface.product_info(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<GroceryProductInfo>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(@NonNull Call<GroceryProductInfo> call, @NonNull Response<GroceryProductInfo> response) {
                            if (response.isSuccessful()) {

                                GroceryProductInfo groceryProductApi = response.body();

                                GroceryProductDataSet productDataSet = groceryProductApi.getProduct();

                                if (productDataSet != null) {

                                    mProductName = productDataSet.item_name;
                                    mProductDescription = productDataSet.description;
                                    mProductImage = productDataSet.picture;
                                    mProductItemPrice = productDataSet.price;
                                    mProductPriceDiscount = productDataSet.discount;

                                    ArrayList<Object> mObject = new ArrayList<>();
                                    ArrayList<String> mTempList = new ArrayList<>();

                                    //Dummy values for UI structure !!!.
                                    mObject.add(mTempList);
                                    //mObject.add(true);

                                    ParentListAdapter parentListAdapter = new ParentListAdapter(mObject);
                                    binding.recyclerMenuOptionDialogListParent.setLayoutManager(new LinearLayoutManager(getActivity()));
                                    binding.recyclerMenuOptionDialogListParent.setAdapter(parentListAdapter);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<GroceryProductInfo> call, @NonNull Throwable t) {
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

    public class ParentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final ArrayList<Object> mACartWholeList;

        public ParentListAdapter(ArrayList<Object> cartWholeList) {
            this.mACartWholeList = cartWholeList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == 1) {
                return new TopViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.view_row_top, parent, false));
            } else {
                return new MiddleViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.view_row_middle, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            if (holder.getItemViewType() == 1) {

                TopViewHolder mTopViewHolder = (TopViewHolder) holder;
                mTopViewHolder.mProductTitle.setText(mProductName);
                mTopViewHolder.mProductDescription.setText(mProductDescription);

                if (!mProductPriceDiscount.isEmpty()) {
                    mTopViewHolder.mPriceDiscount.setVisibility(View.VISIBLE);
                    mTopViewHolder.mProductPrice.setText(mProductPriceDiscount);
                    mTopViewHolder.mPriceDiscount.setText(mProductItemPrice);
                    mTopViewHolder.mPriceDiscount.setPaintFlags(mTopViewHolder.mPriceDiscount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                } else {
                    mTopViewHolder.mProductPrice.setText(mProductItemPrice);
                    mTopViewHolder.mPriceDiscount.setVisibility(View.GONE);
                }

                AppFunctions.imageLoaderUsingGlide(mProductImage, mTopViewHolder.mProductImg, getActivity());

                mTopViewHolder.mQty.setText(mProductMinQty);
                mTopViewHolder.mDecrease.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // //Log.e("mDecrease","called");

                        int qty = Integer.parseInt(mTopViewHolder.mQty.getText().toString());
                        if (qty > Integer.parseInt(mProductMinQty)) {
                            // //Log.e("mDecrease","if -> "+qty);
                            qty = qty - 1;
                            mTopViewHolder.mQty.setText(String.valueOf(qty));
                            mCurrentQty = String.valueOf(qty);

                            if (getActivity() != null) {
                                String mInitialString = getActivity().getString(R.string.add) + " " + qty + " " + getActivity().getString(R.string.to_cart);
                                binding.btnRestaurantMenuOptionDialogAddToCart.setText(mInitialString);
                            }
                        }
                    }
                });
                mTopViewHolder.mIncrease.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //  //Log.e("mIncrease","called");

                        int qty = Integer.parseInt(mTopViewHolder.mQty.getText().toString());
                        //if(qty >= 2){
                        qty = qty + 1;
                        mTopViewHolder.mQty.setText(String.valueOf(qty));
                        mCurrentQty = String.valueOf(qty);

                        if (getActivity() != null) {
                            String mInitialString = getActivity().getString(R.string.add) + " " + qty + " " + getActivity().getString(R.string.to_cart);
                            binding.btnRestaurantMenuOptionDialogAddToCart.setText(mInitialString);
                        }
                        //  //Log.e("mIncrease"," -> "+qty);
                        // }

                    }
                });

            } else if (holder.getItemViewType() == 2) {

                MiddleViewHolder mMiddleViewHolder = (MiddleViewHolder) holder;

            }
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }


        @Override
        public int getItemCount() {
            return mACartWholeList.size();
        }

        @Override
        public int getItemViewType(int position) {

            if (mACartWholeList.get(position) instanceof ArrayList) {
                return 1;
            } else {
                return 2;
            }
        }


        public class TopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final ImageView mProductImg;
            private final TextView mProductTitle;
            private final TextView mProductDescription;
            TextView mProductPrice, mPriceDiscount;
            private final FrameLayout mIncrease;
            private final FrameLayout mDecrease;
            private final TextView mQty;

            public TopViewHolder(View itemView) {
                super(itemView);

                mProductImg = itemView.findViewById(R.id.tv_restaurant_menu_option_Product_image);
                mProductTitle = itemView.findViewById(R.id.tv_restaurant_menu_option_Product_title);
                mProductDescription = itemView.findViewById(R.id.tv_restaurant_menu_option_Product_description);
                mDecrease = itemView.findViewById(R.id.lay_qty_decrease);
                mIncrease = itemView.findViewById(R.id.lay_qty_increase);
                mProductPrice = itemView.findViewById(R.id.tv_Product_title_price);
                mPriceDiscount = itemView.findViewById(R.id.tv_Product_title_price_temp);
                mQty = itemView.findViewById(R.id.tv_option_qty);

            }

            @Override
            public void onClick(View v) {

            }
        }

        public class MiddleViewHolder extends RecyclerView.ViewHolder {

            private final RecyclerView mRecyclerDialogueList;

            public MiddleViewHolder(View itemView) {
                super(itemView);
                mRecyclerDialogueList = itemView.findViewById(R.id.recycler_menu_option_dialog_list);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int mMenuOptionViewId = v.getId();
        if (mMenuOptionViewId == R.id.btn_restaurant_menu_option_dialog_add_to_cart) {
            //Log.e("onClick: ","here" );
            if (restaurantStatus != null && (restaurantStatus.equals("0") || restaurantStatus.equals("2"))) {
                //Currently vendor closed or busy so to show message to user
                //and perform add to cart :-
                toPerformAddToCart(false);
            } else {
                //here restaurant is open or busy :-
                add_to_cart("0");
            }
            mIsProductWithOption = false;
        }
    }

    private void toPerformAddToCart(Boolean isWithOption) {

        String m_VENDORName = restaurantName;
        String m_PRODUCTName = mProductName;
        if (getActivity() != null) {
            String mFINALMsg = m_VENDORName + " " + getActivity().getResources().getString(R.string.pd_is_not_available) + " " +
                    m_PRODUCTName + " " + getActivity().getResources().getString(R.string.pd_at_this_time);

            // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            alertDialogBuilder
                    .setMessage(mFINALMsg)
                    .setCancelable(false)
                    .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (isWithOption) {
                                add_to_cart("1");
                            } else {
                                add_to_cart("0");
                            }
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

    private void add_to_cart(String with_option) {
        mProgressDialog.show();
        if (AppFunctions.networkAvailabilityCheck(activity)) {


            mAddToCartObject = new JSONObject();
            JSONObject product_obj = new JSONObject();
            JSONArray product_array = new JSONArray();
            JSONObject option_object = new JSONObject();
            try {
                product_obj.put(DefaultNames.product_id, mProductId);
                product_obj.put(DefaultNames.quantity, mCurrentQty);
                if (with_option.equals("1")) {
                    JSONObject option_obj = new JSONObject();
                    ArrayList<UserProductOptionDataSet> set = TempOptionDB.getInstance(getActivity()).getAllOptionsList();
                    for (int i = 0; i < set.size(); i++) {
                        if (set.get(i).getOptionType().equals("1")) {
                            option_obj.put(set.get(i).getParentOptionId(), set.get(i).getOptionId());
                        } else if (set.get(i).getOptionType().equals("2")) {
                            JSONArray mOptionArray = new JSONArray();
                            if (!option_obj.isNull(set.get(i).getParentOptionId())) {
                                JSONArray mTempOptionValueArray = option_obj.getJSONArray(set.get(i).getParentOptionId());
                                mTempOptionValueArray.put(mTempOptionValueArray.length(), set.get(i).getOptionId());
                                option_obj.put(set.get(i).getParentOptionId(), mTempOptionValueArray);
                            } else {
                                mOptionArray.put(set.get(i).getOptionId());
                                option_obj.put(set.get(i).getParentOptionId(), mOptionArray);
                            }
                        }
                    }
                    product_obj.put(DefaultNames.option, option_obj);

                } else {
                    product_obj.put(DefaultNames.option, option_object);
                }
                product_array.put(product_obj);
                mAddToCartObject.put(DefaultNames.products, product_array);
                mAddToCartObject.put(DefaultNames.latitude, restaurantLatitude);
                mAddToCartObject.put(DefaultNames.longitude, restaurantLongitude);
                mAddToCartObject.put(DefaultNames.vendor_id, restaurantId);
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

                // Log.e("add_to_cart().Post ",mAddToCartObject.toString());

                /*if(getActivity() != null){
                    if (UserDetailsDB.getInstance(getActivity()).isUserLoggedIn()) {
                    }else {

                        AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                        Intent intent = new Intent(getActivity(), AppLogin.class);
                        getActivity().startActivity(intent);

                    }
                }*/


                retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                String mCustomerAuthorization = "";
                if (AppFunctions.isUserLoggedIn(getActivity())) {
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
                                                delete_cart_dialog(obj.getString("error_warning"), with_option);
                                            }
                                        }
                                    } else {
                                        if (!obj.isNull("error_warning")) {
                                            if (!obj.getString("error_warning").isEmpty()) {
                                                getDialog(obj.getString("error_warning"), false);
                                            }
                                        }
                                    }
                                }
                                if (!obj.isNull("success")) {
                                    JSONObject jsonObject = obj.getJSONObject("success");
                                    if (!jsonObject.isNull("message")) {
                                        if (!jsonObject.getString("message").isEmpty()) {
                                            // getDialog(jsonObject.getString("message"), true);
                                        }
                                    }
                                    if (!obj.isNull("error_warning")) {
                                        if (!obj.getString("error_warning").isEmpty()) {
                                            getDialog(obj.getString("error_warning"), false);
                                        }else {
                                            getParentFragmentManager().popBackStack();
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

    private void delete_cart(String option) {
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
                object.put(DefaultNames.vendor_id, restaurantId);

                RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());
                retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                String mCustomerAuthorization = "";
                if (AppFunctions.isUserLoggedIn(getActivity())) {
                    mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                }

                Call<String> call = retrofitInterface.clear_cart(mCustomerAuthorization, body);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            add_to_cart(option);
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

    private void delete_cart_dialog(String data, String option) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(data);
        builder.setTitle(activity.getResources().getString(R.string.start_a_new_basket));
        builder.setPositiveButton(activity.getResources().getString(R.string.agree), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete_cart(option);
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

    private void getDialog(String data, Boolean isAddToCartSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(data);
        if (isAddToCartSuccess) {
            builder.setCancelable(false);
        } else {
            builder.setCancelable(true);
        }
        builder.setPositiveButton(activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (isAddToCartSuccess) {
                    getParentFragmentManager().popBackStack();
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.create();
        builder.show();

    }
}
