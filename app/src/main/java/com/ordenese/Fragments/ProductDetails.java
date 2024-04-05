package com.ordenese.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.ProductOptionValue;
import com.ordenese.DataSets.ProductOptionsData;
import com.ordenese.DataSets.UserProductOptionDataSet;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.TempOptionDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.R;
import com.ordenese.databinding.FragmentProductDetailsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetails extends Fragment implements View.OnClickListener {

    FragmentProductDetailsBinding binding;
    private ArrayList<ProductOptionsData> mProductOptionsList;
    RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private String mCategoryId = "", mProductId = "", restaurantId = "", restaurantName = "", restaurantStatus = "", restaurantLatitude = "", restaurantLongitude = "", mProductItemPrice = "",mProductDiscountPrice="",
            mProductName = "", mProductImage = "", mProductDescription = "", mProductMinQty = "", mCurrentQty = "1", mPriceStatus = "";
    CartInfo cartInfo;
    Activity activity;
    private JSONObject mAddToCartObject;
    Boolean mIsProductWithOption = false;

    public ProductDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            mProductOptionsList = (ArrayList<ProductOptionsData>) getArguments().getSerializable("product_details");
            restaurantId = getArguments().getString("vendor_id");
            restaurantName = getArguments().getString("vendor_name");

            restaurantStatus = getArguments().getString("vendor_status");

            mProductId = getArguments().getString("product_id");
            mProductName = getArguments().getString("product_name");
            mCategoryId = getArguments().getString("category_id");
            restaurantLatitude = getArguments().getString("latitude");
            restaurantLongitude = getArguments().getString("longitude");
            mProductDescription = getArguments().getString("product_desc");
            mProductImage = getArguments().getString("product_image");
//            mProductMinQty = getArguments().getString("product_minimum");
            mProductItemPrice = getArguments().getString("product_price");
            mProductDiscountPrice = getArguments().getString("product_discount_price");
            mPriceStatus = getArguments().getString("price_status");

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
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);

        activity = getActivity();
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        //To delete the product options form TempOptionDB  :-
        TempOptionDB.getInstance(getActivity()).deleteProductOptionDB();

        ArrayList<Object> mObject = new ArrayList<>();

        ArrayList<String> mTempList = new ArrayList<>();

        //Dummy values for UI structure !!!.
        mObject.add(mTempList);
        mObject.add(true);

        ParentListAdapter parentListAdapter = new ParentListAdapter(mObject);
        binding.recyclerMenuOptionDialogListParent.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerMenuOptionDialogListParent.setAdapter(parentListAdapter);

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

        binding.layoutRestaurantMenuOptionDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.progressBarRestaurantMenuOptionDialog.setVisibility(View.GONE);
        binding.btnRestaurantMenuOptionDialogAddToCart.setOnClickListener(this);

        if (mProductOptionsList != null && mProductOptionsList.size() != 0) {
            if (getActivity() != null) {
                binding.btnRestaurantMenuOptionDialogAddToCart.setEnabled(false);
                binding.btnRestaurantMenuOptionDialogAddToCart.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_btn_disable_status));
                binding.btnRestaurantMenuOptionDialogAddToCart.setTextColor(getResources().getColor(R.color.black));
                // BtnCartGotoCheckout.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_success_yellow_border_round_btn));
            }
        } else {
            if (getActivity() != null) {
                binding.btnRestaurantMenuOptionDialogAddToCart.setEnabled(true);
                binding.btnRestaurantMenuOptionDialogAddToCart.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_square_primary_color));
                binding.btnRestaurantMenuOptionDialogAddToCart.setTextColor(getResources().getColor(R.color.white));
                // BtnCartGotoCheckout.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.bg_success_yellow_border_round_btn));
                // mAddToCart.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_btn_disable_status));
            }
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
        cartInfo.cart_info(false, "", "");
        mProgressDialog.cancel();


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
                if (mPriceStatus.equals("1")) {
//                    if (mProductDiscountPrice != null && !mProductDiscountPrice.equals("")) {
//                        mTopViewHolder.mProductPrice.setVisibility(View.VISIBLE);
//                        mTopViewHolder.mProductPrice.setText(mProductDiscountPrice);
//
//                        mTopViewHolder.mProductDiscountPrice.setText(mProductItemPrice);
//                        mTopViewHolder.mProductPrice.setPaintFlags(mTopViewHolder.mProductDiscountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                    } else {
//                        mTopViewHolder.mProductPrice.setVisibility(View.GONE);
//                        mTopViewHolder.mProductPrice.setPaintFlags(0);
//                        mTopViewHolder.mProductDiscountPrice.setText(mProductItemPrice);
//                    }

                    if(mProductDiscountPrice != null && !mProductDiscountPrice.equals("")){
                        mTopViewHolder.mProductDiscountPrice.setVisibility(View.VISIBLE);
                        mTopViewHolder.mProductDiscountPrice.setText(mProductItemPrice);
                        mTopViewHolder.mProductDiscountPrice.setPaintFlags(mTopViewHolder.mProductDiscountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        mTopViewHolder.mProductPrice.setText(mProductDiscountPrice);
                    }else {
                        mTopViewHolder.mProductDiscountPrice.setVisibility(View.GONE);
                        mTopViewHolder.mProductDiscountPrice.setPaintFlags(0);
                        mTopViewHolder.mProductPrice.setText(mProductItemPrice);
                    }
                } else {
                    mTopViewHolder.mProductPrice.setText(getActivity().getResources().getString(R.string.price_on_selection));
                }
                AppFunctions.imageLoaderUsingGlide(mProductImage, mTopViewHolder.mProductImg, getActivity());
                // Glide.with(activity).load(R.drawable.x_breakfast_400_300_01).into(mTopViewHolder.mProductImg);

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

                if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                    if (mProductOptionsList != null && mProductOptionsList.size() > 0) {
                        mMiddleViewHolder.mProductOptionsAdapter = new ProductOptionsAdapter(getActivity(), mProductOptionsList);
                        mMiddleViewHolder.mRecyclerDialogueList.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mMiddleViewHolder.mRecyclerDialogueList.setAdapter(mMiddleViewHolder.mProductOptionsAdapter);
                        binding.btnRestaurantMenuOptionDialogAddToCart.setVisibility(View.VISIBLE);
                    }
                } else {
                    FragmentTransaction mFT = getFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    mFT.replace(R.id.layout_home_restaurant_body, mNetworkAnalyser, "mNetworkAnalyser");
                    mFT.addToBackStack("mNetworkAnalyser");
                    mFT.commit();
                    getParentFragmentManager().popBackStack();
                }
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
            private final TextView mProductPrice;
            private final FrameLayout mIncrease;
            private final FrameLayout mDecrease;
            private final TextView mQty;
            private final TextView mProductDiscountPrice;

            public TopViewHolder(View itemView) {
                super(itemView);

                mProductImg = itemView.findViewById(R.id.tv_restaurant_menu_option_Product_image);
                mProductTitle = itemView.findViewById(R.id.tv_restaurant_menu_option_Product_title);
                mProductDescription = itemView.findViewById(R.id.tv_restaurant_menu_option_Product_description);
                mDecrease = itemView.findViewById(R.id.lay_qty_decrease);
                mIncrease = itemView.findViewById(R.id.lay_qty_increase);
                mProductPrice = itemView.findViewById(R.id.tv_Product_title_price);
                mProductDiscountPrice = itemView.findViewById(R.id.tv_Product_title_price_temp);
                mQty = itemView.findViewById(R.id.tv_option_qty);

            }

            @Override
            public void onClick(View v) {

            }
        }

        public class MiddleViewHolder extends RecyclerView.ViewHolder {

            private final RecyclerView mRecyclerDialogueList;
            private ProductOptionsAdapter mProductOptionsAdapter;

            public MiddleViewHolder(View itemView) {
                super(itemView);
                mRecyclerDialogueList = itemView.findViewById(R.id.recycler_menu_option_dialog_list);
            }
        }
    }

    public class ProductOptionsAdapter extends RecyclerView.Adapter<ProductOptionsAdapter.ViewHolder> {

        private final ArrayList<ProductOptionsData> mMenuItemOptionsList;
        private final Context mContext;
        private MenuOptionsValuesAdapter mMenuOptionsValuesAdapter;

        private boolean mIsChildVisibled = true;

        public ProductOptionsAdapter(Context context, ArrayList<ProductOptionsData> menuItemOptionsList) {
            this.mContext = context;
            this.mMenuItemOptionsList = menuItemOptionsList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.menu_option_dialog_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.mOptionList.setVisibility(View.VISIBLE);
            holder.mOptionTitle.setVisibility(View.VISIBLE);

            //To show are hide the option require image :-
            if (mMenuItemOptionsList.get(position).getRequired().equals("1")) {
                holder.mOptionRequireImg.setText(getActivity().getResources().getString(R.string.required));
            } else {
                holder.mOptionRequireImg.setText("(" + getActivity().getResources().getString(R.string.optional) + ")");
                addToCartButtonEnableCheck();
            }

            holder.mOptionTitle.setText(mMenuItemOptionsList.get(position).getName());

            if (mMenuItemOptionsList.get(position).getType().equals("2")) {
                //To show or hide the minimum and maximum selection range for checkbox option :-
                String mMinimumLimit = mMenuItemOptionsList.get(position).getMinimumLimit();
                String mMaximumLimit = mMenuItemOptionsList.get(position).getMaximumLimit();
                if (!mMinimumLimit.isEmpty() && !mMaximumLimit.isEmpty()) {
                    holder.mOptionSubTitle.setVisibility(View.VISIBLE);
                    String mSetMinMaxTxt = getResources().getString(R.string.menu_option__choose) + " Min-" + mMinimumLimit + " " +
                            getResources().getString(R.string.menu_option__and) + " Max-" + mMaximumLimit + " " +
                            getResources().getString(R.string.menu_option__items);
                    holder.mOptionSubTitle.setText(mSetMinMaxTxt);
                } else {
                    holder.mOptionSubTitle.setVisibility(View.GONE);
                }
            } else if (mMenuItemOptionsList.get(position).getType().equals("1")) {
                holder.mOptionSubTitle.setVisibility(View.VISIBLE);
                // String mSetSubTitle = "(" + getResources().getString(R.string.menu_option__choose_any_one_option) + ")";
                String mSetSubTitle = getResources().getString(R.string.menu_option__choose_any_one_option);
                holder.mOptionSubTitle.setText(mSetSubTitle);
            } else {
                holder.mOptionSubTitle.setVisibility(View.GONE);
            }

            mMenuOptionsValuesAdapter = new MenuOptionsValuesAdapter(mContext, mMenuItemOptionsList.get(position).getProductOptionValues(),
                    mMenuItemOptionsList.get(position).getType(), mMenuItemOptionsList.get(position).getProductOptionId(), position);
            holder.mOptionList.setLayoutManager(new LinearLayoutManager(getActivity()));
            holder.mOptionList.setAdapter(mMenuOptionsValuesAdapter);

            if (mMenuItemOptionsList.get(position).getProductOptionValues().size() - 1 == position) {
                holder.view_option.setVisibility(View.GONE);
            } else {
                holder.view_option.setVisibility(View.VISIBLE);
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
            return mMenuItemOptionsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final TextView mOptionTitle;
            private final TextView mOptionSubTitle;
            private final LinearLayout mOptionRow;
            private final TextView mOptionRequireImg;
            private final RecyclerView mOptionList;
            View view_option;

            public ViewHolder(View itemView) {
                super(itemView);

                mOptionTitle = itemView.findViewById(R.id.tv_menu_option_dialog_option_title);
                mOptionSubTitle = itemView.findViewById(R.id.tv_menu_option_dialog_option_sub_title);
                mOptionSubTitle.setVisibility(View.GONE);
                mOptionRequireImg = itemView.findViewById(R.id.img_menu_option_dialog_option_require);
                mOptionRow = itemView.findViewById(R.id.lay_menu_option_dialog_row);
                view_option = itemView.findViewById(R.id.view_option);
                mOptionList = itemView.findViewById(R.id.recycler_menu_option_dialog_option_list);

            }

            @Override
            public void onClick(View v) {

            }
        }

        public class MenuOptionsValuesAdapter extends RecyclerView.Adapter<MenuOptionsValuesAdapter.ViewHolder> {

            private final ArrayList<ProductOptionValue> mMenuOptionsValueList;
            private final Context mContext;
            private final String mValueType;
            private final String mParentOptionId;
            private RadioButton mTempRadioBtnOptionValue;
            private final CheckBox mTempCheckBoxOptionValue;
            int mParentPosition;
            private int selectedPosition = 0;
            private boolean mFirstTime = true;

            public MenuOptionsValuesAdapter(Context context, ArrayList<ProductOptionValue> menuOptionsValueList,
                                            String valueType, String parentOptionId,
                                            int parentPosition) {
                this.mContext = context;
                this.mMenuOptionsValueList = menuOptionsValueList;
                this.mValueType = valueType;
                this.mParentPosition = parentPosition;
                this.mParentOptionId = parentOptionId;
                mTempRadioBtnOptionValue = null;
                mTempCheckBoxOptionValue = null;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.menu_option_dialog_value_row, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {

                String mTempParenPosition = String.valueOf(mParentPosition);
                holder.mParentIndex.setText(mTempParenPosition);

                if (mValueType.equals("1")) {

                    //To visible top liner for radio button option :-
                    // holder.mTopLine.setVisibility(View.VISIBLE);
                    // holder.mBottomLine.setVisibility(View.GONE);
                    holder.mOptionValueCheckBoxRow.setVisibility(View.GONE);
                    holder.mOptionValueRadioBtnRow.setVisibility(View.VISIBLE);
//                    holder.mRadioBtnOptionValue.setText(mMenuOptionsValueList.get(position).getName());
                    holder.option_value_name_radio.setText(mMenuOptionsValueList.get(position).getName());

                    String mPrice = mMenuOptionsValueList.get(position).getPrice();
                    if (!mPrice.equals("false") && !mPrice.equals("")) {
                        holder.mRadioBtnPrice.setText("(+" + mPrice + " )");
                    } else {
                        holder.mRadioBtnPrice.setText("");
                    }
                    ProductOptionValue currentItem = mMenuOptionsValueList.get(position);

                    holder.mRadioBtnOptionValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = holder.getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                selectedPosition = position;
                                notifyDataSetChanged();
                            }
                            holder.mRadioBtnOptionValue.setVisibility(View.GONE);
                            holder.mRadioBtnOptionValueChecked.setVisibility(View.VISIBLE);

                            if (TempOptionDB.getInstance(getActivity()).isOptionExists(restaurantId, mCategoryId, mProductId, mParentOptionId)) {

                              /*  //Log.e("*****************", "******************");
                                Log.d(" Single Option   ", "    Exists is replaced by another ...   ");
                                //Log.e("*****************", "******************");*/

                                UserProductOptionDataSet mUserProductOptionDs = new UserProductOptionDataSet();

                                mUserProductOptionDs.setOptionIndex(holder.mParentIndex.getText().toString());
                                mUserProductOptionDs.setBranchId(restaurantId);
                                mUserProductOptionDs.setSectionId(mCategoryId);
                                mUserProductOptionDs.setProductId(mProductId);
                                mUserProductOptionDs.setParentOptionId(mParentOptionId);
                                mUserProductOptionDs.setOptionId(mMenuOptionsValueList.get(position).getOptionValueId());
                                mUserProductOptionDs.setOptionType("1"); //this_is_single_option
                                mUserProductOptionDs.setPrice("0");

                                TempOptionDB.getInstance(getActivity()).updateProductOption(mUserProductOptionDs, restaurantId, mCategoryId, mProductId, mParentOptionId);

                                addToCartButtonEnableCheck();

                            } else {

                                /*//Log.e("*****************", "******************");
                                Log.d(" Single Option   ", "   Newly Added ...   ");
                                //Log.e("*****************", "******************");*/

                                UserProductOptionDataSet mUserProductOptionDs = new UserProductOptionDataSet();

                                mUserProductOptionDs.setOptionIndex(holder.mParentIndex.getText().toString());
                                mUserProductOptionDs.setBranchId(restaurantId);
                                mUserProductOptionDs.setSectionId(mCategoryId);
                                mUserProductOptionDs.setProductId(mProductId);
                                mUserProductOptionDs.setParentOptionId(mParentOptionId);
                                mUserProductOptionDs.setOptionId(mMenuOptionsValueList.get(position).getOptionValueId());
                                mUserProductOptionDs.setOptionType("1"); //this_is_single_option
                                mUserProductOptionDs.setPrice("0");

                                TempOptionDB.getInstance(getActivity()).addProductOption(mUserProductOptionDs);

                                addToCartButtonEnableCheck();

                            }
                        }
                    });

                    holder.mRadioBtnOptionValueChecked.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.mRadioBtnOptionValue.setVisibility(View.VISIBLE);
                            holder.mRadioBtnOptionValueChecked.setVisibility(View.GONE);

                            if (TempOptionDB.getInstance(getActivity()).isOptionExists(restaurantId, mCategoryId, mProductId, mParentOptionId)) {

                                // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                                //Log.e("*****************", "******************");
                                Log.d(" Multi Option   ", "    Exists is Removed  ...   ");
                                //Log.e("*****************", "******************");
                                holder.mCheckBoxOptionValue.setChecked(false);
                                TempOptionDB.getInstance(getActivity()).deleteProductOption(restaurantId, mCategoryId, mProductId, mParentOptionId);

                                addToCartButtonEnableCheck();
                                // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                            }
                        }
                    });
                    if (!mFirstTime) {
                        if (position == selectedPosition) {
                            holder.mRadioBtnOptionValue.setVisibility(View.GONE);
                            holder.mRadioBtnOptionValueChecked.setVisibility(View.VISIBLE);
                        } else {
                            holder.mRadioBtnOptionValue.setVisibility(View.VISIBLE);
                            holder.mRadioBtnOptionValueChecked.setVisibility(View.GONE);
                        }

                    } else{

                        mFirstTime = false;
                    }



                    // //Log.e(position+" R PRICE",mMenuOptionsValueList.get(position).getPrice());
//                    holder.mRadioBtnOptionValue.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                            //   //Log.e( "holder.mOptionValueRadioBtnRow ","clicked");
//                            if (mTempRadioBtnOptionValue != null) {
//                                //  //Log.e( " mTempRadioBtnOptionValue ","not null...");
//                                mTempRadioBtnOptionValue.setChecked(false);
//                                mTempRadioBtnOptionValue = null;
//                            }/*else {
//                                //Log.e(" mTempRadioBtnOptionValue ","null !");
//                             }*/
//                            // String mFinalPrice = getFinalPrice(position);
//
//                            if (TempOptionDB.getInstance(getActivity()).isOptionExists(restaurantId, mCategoryId, mProductId, mParentOptionId)) {
//
//                              /*  //Log.e("*****************", "******************");
//                                Log.d(" Single Option   ", "    Exists is replaced by another ...   ");
//                                //Log.e("*****************", "******************");*/
//
//                                UserProductOptionDataSet mUserProductOptionDs = new UserProductOptionDataSet();
//
//                                mUserProductOptionDs.setOptionIndex(holder.mParentIndex.getText().toString());
//                                mUserProductOptionDs.setBranchId(restaurantId);
//                                mUserProductOptionDs.setSectionId(mCategoryId);
//                                mUserProductOptionDs.setProductId(mProductId);
//                                mUserProductOptionDs.setParentOptionId(mParentOptionId);
//                                mUserProductOptionDs.setOptionId(mMenuOptionsValueList.get(position).getOptionValueId());
//                                mUserProductOptionDs.setOptionType("1"); //this_is_single_option
//                                mUserProductOptionDs.setPrice("0");
//
//                                TempOptionDB.getInstance(getActivity()).updateProductOption(mUserProductOptionDs, restaurantId, mCategoryId, mProductId, mParentOptionId);
//
//                                addToCartButtonEnableCheck();
//
//                            } else {
//
//                                /*//Log.e("*****************", "******************");
//                                Log.d(" Single Option   ", "   Newly Added ...   ");
//                                //Log.e("*****************", "******************");*/
//
//                                UserProductOptionDataSet mUserProductOptionDs = new UserProductOptionDataSet();
//
//                                mUserProductOptionDs.setOptionIndex(holder.mParentIndex.getText().toString());
//                                mUserProductOptionDs.setBranchId(restaurantId);
//                                mUserProductOptionDs.setSectionId(mCategoryId);
//                                mUserProductOptionDs.setProductId(mProductId);
//                                mUserProductOptionDs.setParentOptionId(mParentOptionId);
//                                mUserProductOptionDs.setOptionId(mMenuOptionsValueList.get(position).getOptionValueId());
//                                mUserProductOptionDs.setOptionType("1"); //this_is_single_option
//                                mUserProductOptionDs.setPrice("0");
//
//                                TempOptionDB.getInstance(getActivity()).addProductOption(mUserProductOptionDs);
//
//                                addToCartButtonEnableCheck();
//
//                            }
//
//                            holder.mRadioBtnOptionValue.setChecked(true);
//                            mTempRadioBtnOptionValue = holder.mRadioBtnOptionValue;
//
//
//                        }
//                    });
                } else if (mValueType.equals("2")) {

                    //To visible top liner for check box option :-
                    // holder.mTopLine.setVisibility(View.VISIBLE);
                    //  holder.mBottomLine.setVisibility(View.GONE);
                    holder.mOptionValueCheckBoxRow.setVisibility(View.VISIBLE);
                    holder.mOptionValueRadioBtnRow.setVisibility(View.GONE);
//                    holder.mCheckBoxOptionValue.setText(mMenuOptionsValueList.get(position).getName());
                    holder.option_value_name_chk.setText(mMenuOptionsValueList.get(position).getName());

                    String mPrice = mMenuOptionsValueList.get(position).getPrice();
                    if (!mPrice.equals("false") && !mPrice.equals("")) {
                        holder.mCheckBoxPrice.setText("(+" + mPrice + " )");
                    } else {
                        holder.mCheckBoxPrice.setText("");
                    }

                    holder.mCheckBoxOptionValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String mOptionValueId = mMenuOptionsValueList.get(position).getOptionValueId();

                            if (TempOptionDB.getInstance(getActivity()).isOptionValueExists(restaurantId, mCategoryId, mProductId, mParentOptionId, mOptionValueId)) {

                                // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

                                //Log.e("*****************", "******************");
                                Log.d(" Multi Option   ", "    Exists is Removed  ...   ");
                                //Log.e("*****************", "******************");

                                holder.mCheckBoxOptionValue.setChecked(false);
                                TempOptionDB.getInstance(getActivity()).deleteProductOptionValueId(restaurantId, mCategoryId, mProductId, mParentOptionId, mOptionValueId);

                                addToCartButtonEnableCheck();


                                // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

                            } else {

                                // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                                //Log.e("*****************", "******************");
                                Log.d(" Multi Option   ", "   Newly Added ...   ");
                                //Log.e("*****************", "******************");


                                //To show or hide the minimum and maximum selection range for checkbox option :-
//                                 String mMinimumLimit = mMenuItemOptionsList.get(0).getItemTitleList().get(position).getMinimumLimit();
                                int mTempParentPosition = Integer.valueOf(holder.mParentIndex.getText().toString());
                                String mMaximumLimit = mMenuItemOptionsList.get(mTempParentPosition).getMaximumLimit();
                                String mMinimumLimit = mMenuItemOptionsList.get(mTempParentPosition).getMinimumLimit();

                                Log.e("onClick: ", mMaximumLimit + "/" + mMinimumLimit);

                                Boolean mIsMinMaxZero;
                                mIsMinMaxZero = mMenuItemOptionsList.get(mTempParentPosition).getMinimumLimit().equals("0") &&
                                        mMenuItemOptionsList.get(mTempParentPosition).getMaximumLimit().equals("0");

                                if (/*!mMinimumLimit.isEmpty() &&*/ !mMaximumLimit.isEmpty()) {

                                    //To get the parent product option id form item title list :-
                                    String mParentOptionId = mProductOptionsList.get(mTempParentPosition).getProductOptionId();

                                    //  //Log.e("*****mMaximum Limit*****", mMaximumLimit);
                                    int mMultiOptionCount = TempOptionDB.getInstance(getActivity()).getProductOptionCount(restaurantId, mCategoryId, mProductId, mParentOptionId);
                                    //  //Log.e("*****DB MultiOptCount*****", String.valueOf(mMultiOptionCount));
                                    // String mFinalPrice = getFinalPrice(position);

                                    //To check the option selection should not exists the maximum count :-
                                    //NOTE : The minimum count will be check at add to cart process for this product.
//                                    if (mMultiOptionCount == Integer.parseInt(mMaximumLimit) || mIsMinMaxZero) {
                                    if (mMultiOptionCount < Integer.parseInt(mMaximumLimit) || mIsMinMaxZero) {

//                                          holder.mCheckBoxOptionValue.setChecked(true);

                                        //If option not exists then add the option :-
                                        UserProductOptionDataSet userProductOptionDs = new UserProductOptionDataSet();

                                        userProductOptionDs.setOptionIndex(holder.mParentIndex.getText().toString());
                                        userProductOptionDs.setBranchId(restaurantId);
                                        userProductOptionDs.setSectionId(mCategoryId);
                                        userProductOptionDs.setProductId(mProductId);
                                        userProductOptionDs.setParentOptionId(mParentOptionId);
                                        userProductOptionDs.setOptionId(mMenuOptionsValueList.get(position).getOptionValueId());
                                        userProductOptionDs.setOptionType("2"); //this_is_multi_option
                                        userProductOptionDs.setPrice("0");
                                        TempOptionDB.getInstance(getActivity()).addProductOption(userProductOptionDs);

                                        addToCartButtonEnableCheck();

                                    } else {
                                        holder.mCheckBoxOptionValue.setChecked(false);
                                    }
                                } else {
                                    //If option not exists then add the option :-
                                    UserProductOptionDataSet userProductOptionDs = new UserProductOptionDataSet();

                                    userProductOptionDs.setOptionIndex(holder.mParentIndex.getText().toString());
                                    userProductOptionDs.setBranchId(restaurantId);
                                    userProductOptionDs.setSectionId(mCategoryId);
                                    userProductOptionDs.setProductId(mProductId);
                                    userProductOptionDs.setParentOptionId(mParentOptionId);
                                    userProductOptionDs.setOptionId(mMenuOptionsValueList.get(position).getOptionValueId());
                                    userProductOptionDs.setOptionType("2"); //this_is_multi_option
                                    userProductOptionDs.setPrice("0");
                                    TempOptionDB.getInstance(getActivity()).addProductOption(userProductOptionDs);

                                    addToCartButtonEnableCheck();
                                }
                                // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                            }
                        }
                    });

                } else {

                    holder.mOptionValueCheckBoxRow.setVisibility(View.GONE);
                    holder.mOptionValueRadioBtnRow.setVisibility(View.GONE);
                }

                if (position == mMenuOptionsValueList.size() - 1) {
                    holder.mBottomLine.setVisibility(View.GONE);
                } else {
                    holder.mBottomLine.setVisibility(View.VISIBLE);
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
                return mMenuOptionsValueList.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                private final LinearLayout mOptionValueCheckBoxRow;
                private final LinearLayout mOptionValueRadioBtnRow;
                private final CheckBox mCheckBoxOptionValue;
//                private final RadioButton mRadioBtnOptionValue;
                private final ImageView mRadioBtnOptionValue,mRadioBtnOptionValueChecked;
                private final TextView mCheckBoxPrice/*, mCheckBoxOfferPrice*/;
                private final TextView mRadioBtnPrice/*, mRadioBtnOfferPrice*/;
                private final TextView mParentIndex;
                private final TextView option_value_name_chk;
                private final TextView option_value_name_radio;
                private View mBottomLine;

                public ViewHolder(View itemView) {
                    super(itemView);

                    mOptionValueCheckBoxRow = itemView.findViewById(R.id.layout_checkbox_menu_option_dialog_value_container);
                    mOptionValueCheckBoxRow.setVisibility(View.GONE);
                    mOptionValueRadioBtnRow = itemView.findViewById(R.id.layout_radio_btn_menu_option_dialog_value_container);
                    mOptionValueRadioBtnRow.setVisibility(View.GONE);

                    mCheckBoxOptionValue = itemView.findViewById(R.id.checkbox_menu_option_dialog_value);
                    mRadioBtnOptionValue = itemView.findViewById(R.id.radio_btn_menu_option_dialog_value);
                    mRadioBtnOptionValueChecked = itemView.findViewById(R.id.radio_btn_menu_option_dialog_value_checked);


                    option_value_name_chk = itemView.findViewById(R.id.option_value_name);
                    option_value_name_radio = itemView.findViewById(R.id.option_value_name_radio);

                    mCheckBoxPrice = itemView.findViewById(R.id.tv_checkbox_menu_option_dialog_price);
                    //   mCheckBoxOfferPrice = (TextView) itemView.findViewById(R.id.tv_checkbox_menu_option_dialog_offer_price);
                    //  mCheckBoxOfferPrice.setVisibility(View.GONE);
                    mRadioBtnPrice = itemView.findViewById(R.id.tv_radio_btn_menu_option_dialog_price);
                    // mRadioBtnOfferPrice = (TextView) itemView.findViewById(R.id.tv_radio_btn_menu_option_dialog_offer_price);
                    // mRadioBtnOfferPrice.setVisibility(View.GONE);
                    mParentIndex = itemView.findViewById(R.id.tv_menu_option_dialog_parent_index);
                    mParentIndex.setVisibility(View.GONE);

                    // mTopLine = itemView.findViewById(R.id.view_menu_option_dialog_top_liner);
                    // mTopLine.setVisibility(View.GONE);

                    mBottomLine = itemView.findViewById(R.id.mBottomLine);
                    mBottomLine.setVisibility(View.GONE);

                }

                @Override
                public void onClick(View v) {

                }
            }

        }
    }


    @Override
    public void onClick(View v) {
        int mMenuOptionViewId = v.getId();
        if (mMenuOptionViewId == R.id.btn_restaurant_menu_option_dialog_add_to_cart) {

            //Log.e("onClick: ","here" );
            if (mProductOptionsList != null && mProductOptionsList.size() != 0) {
                int mItemListSize = mProductOptionsList.size();
                int mOptionRequireCount = 0;
                String mIsRequiredSelected = "";
                for (int addCart = 0; addCart < mItemListSize; addCart++) {
                    if (mProductOptionsList.get(addCart).getRequired().equals(DefaultNames.optionRequired)) {
                        //mOptionRequireCount++;

                        //To get the parent product option id form item title list :-
                        String mParentOptionId = mProductOptionsList.get(addCart).getProductOptionId();

                        // restaurantId,mCategoryId,mProductId,mParentOptionId

                        //To check , Is require option is selected or not :-
                        Boolean mIsExists = TempOptionDB.getInstance(getActivity()).isOptionExists(restaurantId, mCategoryId, mProductId, mParentOptionId); // ------- >>>>>>>>>>>>>>>
                        if (!mIsExists) {

                            //Here , require option is not selected :-
                            // AppFunctions.toastShort(getActivity(), getResources().getString(R.string.please_select_required_options));
                            AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.please_select_required_options));
                            ////Log.e("BreakAtIndex", String.valueOf(addCart));
                            mIsRequiredSelected = DefaultNames.required_notSelected;
                            break;
                        } else {
                            //Here , require option is selected :-
                            //So to check , If option is multi option type.Then check the Min/Max condition for multi option selection:-
                            if (mProductOptionsList.get(addCart).getType().equals("2")) {
//                            if (mProductOptionsList.get(addCart).getType().equals(DefaultNames.checkbox)) {
                                //To check the minimum and maximum selection range for checkbox option :-
                                String mMinimumLimit = mProductOptionsList.get(addCart).getMinimumLimit();
                                String mMaximumLimit = mProductOptionsList.get(addCart).getMaximumLimit();
                                if (!mMinimumLimit.isEmpty() && !mMaximumLimit.isEmpty()) {

                                    // ------- >>>>>>>>>>>>>>>

                                    int mCheckBoxOptionsSelectedCount = TempOptionDB.getInstance(getActivity()).getProductOptionCount(restaurantId, mCategoryId, mProductId, mParentOptionId);
                                    int mTempMinimumLimit = Integer.valueOf(mMinimumLimit);
                                    int mTempMaximumLimit = Integer.valueOf(mMaximumLimit);

                                    Boolean mIsMinMaxZero;
                                    mIsMinMaxZero = mTempMinimumLimit == 0 && mTempMaximumLimit == 0;

                                    if ((mCheckBoxOptionsSelectedCount >= mTempMinimumLimit &&
                                            mCheckBoxOptionsSelectedCount <= mTempMaximumLimit) || mIsMinMaxZero) {
                                        mIsRequiredSelected = DefaultNames.required_selected;

                                    } else {
                                        String mOptionTitle = mProductOptionsList.get(addCart).getName();
                                        // AppFunctions.toastShort(getActivity(), getResources().getString(R.string.menu_option__choose_minimum) + " " + mMinimumLimit + " " +
                                        //  getResources().getString(R.string.menu_option__and_maximum) + " " + mMaximumLimit + " " +
                                        // getResources().getString(R.string.menu_option__items_in) + " " + mOptionTitle);

                                        AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.menu_option__choose_minimum) + " " + mMinimumLimit + " " +
                                                getResources().getString(R.string.menu_option__and_maximum) + " " + mMaximumLimit + " " +
                                                getResources().getString(R.string.menu_option__items_in) + " " + mOptionTitle);
                                        mIsRequiredSelected = DefaultNames.required_notSelected;
                                        break;
                                    }
                                }
                            } else {
                                mIsRequiredSelected = DefaultNames.required_selected;
                            }
                        }
                    } else {
                        mIsRequiredSelected = DefaultNames.required_notAvailable;
                    }
                }

                if (!mIsRequiredSelected.isEmpty() && (mIsRequiredSelected.equals(DefaultNames.required_selected) ||
                        mIsRequiredSelected.equals(DefaultNames.required_notAvailable))) {
                    //  AppFunctions.toastShort(getActivity(),"Required Options "+String.valueOf(mOptionRequireCount));
                    //***********************************  1   ************************************
                    if (restaurantStatus != null && (restaurantStatus.equals("0") || restaurantStatus.equals("2"))) {
                        //Currently vendor closed or busy so to show message to user
                        //and perform add to cart :-
                        toPerformAddToCart(true);
                    } else {
                        //here restaurant is open or busy :-
                        add_to_cart("1");
                    }
                    mIsProductWithOption = true;
                }
            } else {
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
                                            //getDialog(jsonObject.getString("message"), true);
                                        }
                                    }
                                    if (!obj.isNull("error_warning")) {
                                        if (!obj.getString("error_warning").isEmpty()) {
                                            getDialog(obj.getString("error_warning"), false);
                                        } else {
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

    private void addToCartButtonEnableCheck() {

        if (mProductOptionsList != null && mProductOptionsList.size() != 0) {

            int mItemListSize = mProductOptionsList.size();

            String mIsRequiredSelected = "";
            for (int addCart = 0; addCart < mItemListSize; addCart++) {
                if (mProductOptionsList.get(addCart).getRequired().equals(DefaultNames.optionRequired)) {

                    //To get the parent product option id form item title list :-
                    String mParentOptionId = mProductOptionsList.get(addCart).getProductOptionId();
                    //To check , Is require option is selected or not :-
                    Boolean mIsExists = TempOptionDB.getInstance(getActivity()).isOptionExists(restaurantId, mCategoryId, mProductId, mParentOptionId); // ------- >>>>>>>>>>>>>>>
                    if (!mIsExists) {
                        //Here , require option is not selected :-
                        //  AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.please_select_required_options));
                        mIsRequiredSelected = DefaultNames.required_notSelected;
                        break;
                    } else {

                        //Here , require option is selected :-
                        //So to check , If option is multi option type.Then check the Min/Max condition for multi option selection:-
                        if (mProductOptionsList.get(addCart).getType().equals("2")) {
//                        if (mProductOptionsList.get(addCart).getType().equals(DefaultNames.checkbox)) {

                            //To check the minimum and maximum selection range for checkbox option :-
                            String mMinimumLimit = mProductOptionsList.get(addCart).getMinimumLimit();
                            String mMaximumLimit = mProductOptionsList.get(addCart).getMaximumLimit();

                            Log.e("addToCartButton", mMaximumLimit + "");
                            Log.e("addToCartButton1 ", mMaximumLimit + "");

                            if (!mMinimumLimit.isEmpty() && !mMaximumLimit.isEmpty()) {
                                int mCheckBoxOptionsSelectedCount = TempOptionDB.getInstance(getActivity()).getProductOptionCount(restaurantId, mCategoryId, mProductId, mParentOptionId);
                                int mTempMinimumLimit = Integer.valueOf(mMinimumLimit);
                                int mTempMaximumLimit = Integer.valueOf(mMaximumLimit);

                                Boolean mIsMinMaxZero;
                                mIsMinMaxZero = mTempMinimumLimit == 0 && mTempMaximumLimit == 0;

                                if ((mCheckBoxOptionsSelectedCount >= mTempMinimumLimit &&
                                        mCheckBoxOptionsSelectedCount <= mTempMaximumLimit) || mIsMinMaxZero) {

                                    mIsRequiredSelected = DefaultNames.required_selected;

                                } else {
                                    String mOptionTitle = mProductOptionsList.get(addCart).getName();

                                    // AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.menu_option__choose_minimum) + " " + mMinimumLimit + " " +
                                    // getResources().getString(R.string.menu_option__and_maximum) + " " + mMaximumLimit + " " +
                                    // getResources().getString(R.string.menu_option__items_in) + " " + mOptionTitle);

                                    mIsRequiredSelected = DefaultNames.required_notSelected;
                                    break;
                                }
                            }
                        } else {
                            mIsRequiredSelected = DefaultNames.required_selected;
                        }
                    }
                } else {
                    mIsRequiredSelected = DefaultNames.required_notAvailable;
                }
            }
            if (!mIsRequiredSelected.isEmpty() && (mIsRequiredSelected.equals(DefaultNames.required_selected) ||
                    mIsRequiredSelected.equals(DefaultNames.required_notAvailable))) {
                if (getActivity() != null) {
                    binding.btnRestaurantMenuOptionDialogAddToCart.setEnabled(true);
                    binding.btnRestaurantMenuOptionDialogAddToCart.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_square_primary_color));
                    binding.btnRestaurantMenuOptionDialogAddToCart.setTextColor(getResources().getColor(R.color.white));
                }
            } else {
                if (getActivity() != null) {
                    binding.btnRestaurantMenuOptionDialogAddToCart.setEnabled(false);
                    binding.btnRestaurantMenuOptionDialogAddToCart.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_btn_disable_status));
                    binding.btnRestaurantMenuOptionDialogAddToCart.setTextColor(getResources().getColor(R.color.black));
                }
            }
        }

    }

}


/*    private void add_to_cart(String with_option) {
        mProgressDialog.show();
        if (AppFunctions.networkAvailabilityCheck(activity)) {

            if (UserDetailsDB.getInstance(getActivity()).isUserLoggedIn()) {


                JSONObject object = new JSONObject();
                JSONObject product_obj = new JSONObject();
                JSONArray product_array = new JSONArray();
                JSONObject option_object = new JSONObject();
                try {
                    product_obj.put("product_id", mProductId);
                    product_obj.put("quantity", mCurrentQty);
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
                        product_obj.put("option", option_obj);
                    } else {
                        product_obj.put("option", option_object);
                    }
                    product_array.put(product_obj);
                    object.put("products", product_array);
                    object.put("latitude", restaurantLatitude);
                    object.put("longitude", restaurantLongitude);
                    object.put("vendor_id", restaurantId);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());
                    retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                   String mCustomerAuthorization = "";
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                        }

                    Call<String> call = retrofitInterface.add_to_cart(mCustomerAuthorization, body);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
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
                                                    getDialog(obj.getString("error_warning"),false);
                                                }
                                            }
                                        }
                                    }
                                    if (!obj.isNull("success")) {
                                        JSONObject jsonObject = obj.getJSONObject("success");
                                        if (!jsonObject.isNull("message")) {
                                            if (!jsonObject.getString("message").isEmpty()) {
                                                getDialog(jsonObject.getString("message"),true);
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
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    mProgressDialog.cancel();
                }




            } else {
                AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                Intent intent = new Intent(getActivity(), AppLogin.class);
                getActivity().startActivity(intent);
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
*/