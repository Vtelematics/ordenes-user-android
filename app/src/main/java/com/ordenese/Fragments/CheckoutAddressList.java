
package com.ordenese.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.SwipeLayout;
import com.ordenese.Activities.AppLogin;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AddressGeocodeDataSet;
import com.ordenese.DataSets.AddressListApi;
import com.ordenese.DataSets.AddressListDataSet;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.UserAddressDataSet;
import com.ordenese.Databases.AddressBookChangeLocationDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.RestaurantAddressDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutAddressList extends Fragment implements View.OnClickListener {

    private View mAddressView;

    private FrameLayout mAppBar;
    private ImageView mAppBarNewAddsImg;
    private LinearLayout mAppBarBackLay, mAppBarBackBtnNewAddsLay;

    private ProgressDialog mProgressDialog;

    private ArrayList<UserAddressDataSet> mTEMPUserAddressList;
    private RecyclerView mAddsList;
    private RecyclerView.LayoutManager mAddsListLayoutMgr;
    private MyAccountAddressListAdapter myAccountAddressListAdapter;

    private UserAddressDataSet mSelectedUserAddressDataSet;

    private ImageView mImgBackBtn, mImgAddAddressBtn;
    private RetrofitInterface retrofitInterface;
    private AddressListApi mAddressListApi;
    private TextView mAddressListEmptyMsg;

    private String mIsAddsBookCallFrom = "";
    private String vendor_id = "";

    public CheckoutAddressList() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAddressView = inflater.inflate(R.layout.checkout_address_list, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        if (getArguments() != null) {
            mIsAddsBookCallFrom = getArguments().getString(DefaultNames.addressBook_callFrom);
            vendor_id = getArguments().getString("Vendor_id");
        }


        // mAppBar = mAddressView.findViewById(R.id.lay_saved_adds_app_bar);

        mImgBackBtn = mAddressView.findViewById(R.id.img_co_al_back);
        mImgBackBtn.setOnClickListener(this);
        mImgAddAddressBtn = mAddressView.findViewById(R.id.img_co_al_add_adds);
        mImgAddAddressBtn.setOnClickListener(this);

        mAddressListEmptyMsg = mAddressView.findViewById(R.id.tv_co_al_saved_adds_adds_list_empty_msg);
        mAddressListEmptyMsg.setVisibility(View.GONE);



       /* mAppBarBackLay = mAddressView.findViewById(R.id.lay_saved_adds_app_bar_back);
        mAppBarBackLay.setOnClickListener(this);

        mAppBarBackLay = mAddressView.findViewById(R.id.lay_saved_adds_app_bar_back);
        mAppBarBackLay.setOnClickListener(this);
        mAppBarBackLay = mAddressView.findViewById(R.id.lay_saved_adds_app_bar_back);
        mAppBarBackLay.setOnClickListener(this);*/



       /* mAppBarBackBtnNewAddsLay = mAddressView.findViewById(R.id.lay_saved_adds_app_bar_new_adds);
        mAppBarBackBtnNewAddsLay.setOnClickListener(this);
        mAppBarNewAddsImg = mAddressView.findViewById(R.id.img_saved_adds_app_bar_new);
        mAppBarNewAddsImg.setOnClickListener(this);*/

        mAddsList = mAddressView.findViewById(R.id.recycler_co_al_saved_adds_adds_list);

        //
        //,


        return mAddressView;
    }

    @Override
    public void onResume() {
        super.onResume();


        if (getActivity() != null) {
            callAddressListApi();
        }


    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onClick(View view) {

        int mId = view.getId();
        if (mId == R.id.img_co_al_back) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        } else if (mId == R.id.img_co_al_add_adds) {
            if (getActivity() != null) {

                if (mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()) {


                    //*********************************************************************************
                    //AppFunctions.msgDialogOk(getActivity(), "", "add address");
                    //The AddressBookChangeLocationDB is used for temporary map page lat long and its address storage
                    //purposes.So whenever we go for CheckoutAddressAddEdit page.then reset the AddressBookChangeLocationDB
                    // details.
                    if (AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                    }
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    CheckoutAddressAddEdit m_CheckoutAddressAddEdit = new CheckoutAddressAddEdit();
                    Bundle mBundle = new Bundle();
                    mBundle.putString(DefaultNames.address_add_or_edit, DefaultNames.address_add_process);
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                        mBundle.putString("Vendor_id",vendor_id);
                        m_CheckoutAddressAddEdit.setArguments(mBundle);
                        mFT.replace(R.id.layout_app_check_out_body, m_CheckoutAddressAddEdit, "m_CheckoutAddressAddEdit");
                        mFT.addToBackStack("m_CheckoutAddressAddEdit");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                        //Its - my account page address book call.
                        mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_MyAccountAddsBook);
                        mBundle.putString("Vendor_id",vendor_id);
                        m_CheckoutAddressAddEdit.setArguments(mBundle);
                        mFT.replace(R.id.layout_app_home_body, m_CheckoutAddressAddEdit, "m_CheckoutAddressAddEdit");
                        mFT.addToBackStack("m_CheckoutAddressAddEdit");
                        mFT.commit();
                    }
                    //*********************************************************************************


                }


            }

        }
    }


    public class MyAccountAddressListAdapter extends RecyclerView.Adapter<MyAccountAddressListAdapter.DataObjectHolder> {

        private final ArrayList<AddressListDataSet> mAddressesList;
        private CardView mSelectedContainer;
        private SwipeLayout mTempSwipeLayout;
        private final int mLastPosition = -1;


        public MyAccountAddressListAdapter(ArrayList<AddressListDataSet> addressesList) {
            this.mAddressesList = addressesList;
        }

        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkout_address_list_row, parent, false);
            return new DataObjectHolder(mView);
        }


        @Override
        public void onBindViewHolder(final DataObjectHolder holder, final int position) {

            String mFName = mAddressesList.get(position).getFirst_name();
            String mLName = mAddressesList.get(position).getLast_name();
            String mName = mFName + " " + mLName;

            String mAdds = "";

            String mBlock = mAddressesList.get(position).getBlock();
            String mStreet = mAddressesList.get(position).getStreet();
            String mBuilding = mAddressesList.get(position).getBuilding_name();
            String mWay = mAddressesList.get(position).getWay();
            String mFloor = mAddressesList.get(position).getFloor();
            String mDoorNo = mAddressesList.get(position).getDoor_no();
            String mMob = getActivity().getResources().getString(R.string.mobile) + ": " + mAddressesList.get(position).getCountry_code() + "-" +
                    mAddressesList.get(position).getMobile();


            String mDeliveryAdds = "";

            if (mBlock != null && !mBlock.isEmpty()) {
                mDeliveryAdds = "" + mBlock;
            }

            if (mStreet != null && !mStreet.isEmpty()) {
                if (mDeliveryAdds.length() > 0) {
                    mDeliveryAdds = mDeliveryAdds + "," + mStreet;
                } else {
                    mDeliveryAdds = "" + mStreet;
                }
            }

            if (mBuilding != null && !mBuilding.isEmpty()) {
                if (mDeliveryAdds.length() > 0) {
                    mDeliveryAdds = mDeliveryAdds + "," + mBuilding;
                } else {
                    mDeliveryAdds = "" + mBuilding;
                }
            }

            if (mWay != null && !mWay.isEmpty()) {
                if (mDeliveryAdds.length() > 0) {
                    mDeliveryAdds = mDeliveryAdds + "," + mWay;
                } else {
                    mDeliveryAdds = "" + mWay;
                }
            }

            String mAddsTYPE = mAddressesList.get(position).getAddress_type();
            //  Log.e("268","mAddsTYPe "+mAddsTYPE);
            if (mAddsTYPE.equals("1")) {
                //Its a house address type :-
                mAdds = getActivity().getResources().getString(R.string.caa_house) + " (" + mAddressesList.get(position).getArea() + ")";
            } else if (mAddsTYPE.equals("2")) {
                //Its a apartment address type :-
                mAdds = getActivity().getResources().getString(R.string.caa_apartment) + " (" + mAddressesList.get(position).getArea() + ")";
            } else {
                //Its a office address type :-
                mAdds = getActivity().getResources().getString(R.string.caa_office) + " (" + mAddressesList.get(position).getArea() + ")";
            }

            /*if (mAddsTYPE.equals("2") || mAddsTYPE.equals("3")) {
                //To show floor and door no field datas only when address type is apartment or office.
                //If address type is house then to hide the floor and door no field data.
                if (mFloor != null && !mFloor.isEmpty()) {
                    if (mDeliveryAdds.length() > 0) {
                        mDeliveryAdds = mDeliveryAdds + "," + mFloor;
                    } else {
                        mDeliveryAdds = "" + mFloor;
                    }
                }
                if (mDoorNo != null && !mDoorNo.isEmpty()) {
                    if (mDeliveryAdds.length() > 0) {
                        mDeliveryAdds = mDeliveryAdds + "," + mDoorNo;
                    } else {
                        mDeliveryAdds = "" + mDoorNo;
                    }
                }
            } else {
                mDeliveryAdds = mDeliveryAdds;
            }*/
            if (mAddsTYPE.equals("2") || mAddsTYPE.equals("3")) {
                //To show floor and door no field datas only when address type is apartment or office.
                //If address type is house then to hide the floor and door no field data.
                if (mFloor != null && !mFloor.isEmpty()) {
                    if (mDeliveryAdds.length() > 0) {
                        if (!mFloor.equals("0")) {
                            mDeliveryAdds = mDeliveryAdds + "," + mFloor;
                        }
                    } else {
                        if (!mFloor.equals("0")) {
                            mDeliveryAdds = "" + mFloor;
                        }
                    }
                }
                if (mDoorNo != null && !mDoorNo.isEmpty()) {
                    if (mDeliveryAdds.length() > 0) {
                        if (!mFloor.equals("0")) {
                            mDeliveryAdds = mDeliveryAdds + "," + mDoorNo;
                        }
                    } else {
                        if (!mFloor.equals("0")) {
                            mDeliveryAdds = "" + mDoorNo;
                        }
                    }
                }
            } else {
                mDeliveryAdds = mDeliveryAdds;
            }

            holder.m__Name.setText(mName);
//            holder.mAddress.setText(mAdds);
            holder.mAddress.setText(mAddressesList.get(position).getAddress());
            holder.mAddressSubDetails.setText(mDeliveryAdds);
            holder.mAddressSubDetailsMob.setText(mMob);


            holder.mParentRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //This CheckOutAddressList.java page called from two type of purposes.
                    //one for my account address book purposes and another one for checkout page
                    //address selection purposes.
                    //The delivery api check only need when checkout page address selection.
                    if (mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty() &&
                            mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        callIsDeliveryAPi(mAddressesList.get(position).getLatitude(), mAddressesList.get(position).getLongitude(),
                                mAddressesList.get(position).getAddress_id(), mAddressesList.get(position).getZone_id());
                    } else {
                        //Its - my account page address book call.
                    }

                }
            });

            holder.mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()) {


                        //*********************************************************************************

                        String mFName = mAddressesList.get(position).getFirst_name();
                        String mLName = mAddressesList.get(position).getLast_name();
                        String mName = mFName + " " + mLName;

                        String mArea = mAddressesList.get(position).getArea();
                        String mArea_id_or_zone_id = mAddressesList.get(position).getZone_id();

                        String mBlock = mAddressesList.get(position).getBlock();
                        String mStreet = mAddressesList.get(position).getStreet();
                        String mBuilding = mAddressesList.get(position).getBuilding_name();
                        String mWay = mAddressesList.get(position).getWay();
                        String mFloor = mAddressesList.get(position).getFloor();
                        String mDoorNo = mAddressesList.get(position).getDoor_no();
                        String mMob = getActivity().getResources().getString(R.string.mobile) + ": " + mAddressesList.get(position).getCountry_code() + "-" +
                                mAddressesList.get(position).getMobile();

                        String mCountry_code = mAddressesList.get(position).getCountry_code();
                        String mMobile = mAddressesList.get(position).getMobile();
                        String mEmail = mAddressesList.get(position).getEmail();

                        String mLatitude = mAddressesList.get(position).getLatitude();
                        String mLongitude = mAddressesList.get(position).getLongitude();
                        String mAddressId = mAddressesList.get(position).getAddress_id();

                        String landline = mAddressesList.get(position).getLandline();
                        String address_type = mAddressesList.get(position).getAddress_type();
                        String additional_direction = mAddressesList.get(position).getAdditional_direction();

                        //   Log.e("345 mAddressId", "" + mAddressId);
                        //   Log.e("346 Latitude", "" + mLatitude);
                        //  Log.e("347 Longitude", "" + mLongitude);
                        //  Log.e("348 address_type", "" + address_type);

                        //The AddressBookChangeLocationDB is used for temporary map page lat long and its address storage
                        //purposes.So whenever we go for CheckoutAddressAddEdit page.then reset the AddressBookChangeLocationDB
                        // details.
                        if (AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0) {
                            AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                        }
                        AreaGeoCodeDataSet mAreaDS = new AreaGeoCodeDataSet();
                        mAreaDS.setmAddress("");
                        mAreaDS.setmLatitude(String.valueOf(mLatitude));
                        mAreaDS.setmLongitude(String.valueOf(mLongitude));
                        mAreaDS.setmAddsNameOnly("");
                        AddressBookChangeLocationDB.getInstance(getActivity()).add(mAreaDS);

                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                        CheckoutAddressAddEdit m_CheckoutAddressAddEdit = new CheckoutAddressAddEdit();
                        Bundle mBundle = new Bundle();
                        mBundle.putString(DefaultNames.address_add_or_edit, DefaultNames.address_edit_process);

                        mBundle.putString(DefaultNames.firstName, mFName);
                        mBundle.putString(DefaultNames.lastName, mLName);
                        mBundle.putString(DefaultNames.area, mArea);
                        mBundle.putString(DefaultNames.zone_id, mArea_id_or_zone_id);
                        mBundle.putString(DefaultNames.block, mBlock);
                        mBundle.putString(DefaultNames.street, mStreet);
                        mBundle.putString(DefaultNames.building, mBuilding);
                        mBundle.putString(DefaultNames.way, mWay);
                        mBundle.putString(DefaultNames.floor, mFloor);
                        mBundle.putString(DefaultNames.door_no, mDoorNo);
                        mBundle.putString(DefaultNames.country_code, mCountry_code);
                        mBundle.putString(DefaultNames.mobile, mMobile);
                        mBundle.putString(DefaultNames.email, mEmail);
                        mBundle.putString(DefaultNames.latitude, mLatitude);
                        mBundle.putString(DefaultNames.longitude, mLongitude);
                        mBundle.putString(DefaultNames.address_id, mAddressId);
                        mBundle.putString(DefaultNames.landline, landline);
                        mBundle.putString(DefaultNames.address_type, address_type);
                        mBundle.putString(DefaultNames.additional_direction, additional_direction);
                        mBundle.putString("Vendor_id",vendor_id);
                        //street,building,way,floor,door_no,country_code,mobile
                        //landline,address_type,additional_direction

                        if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                            //Its for - checkout page address selection
                            mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                            m_CheckoutAddressAddEdit.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_check_out_body, m_CheckoutAddressAddEdit, "m_CheckoutAddressAddEdit");
                            mFT.addToBackStack("m_CheckoutAddressAddEdit");
                            mFT.commit();
                        } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                            //Its - my account page address book call.
                            mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_MyAccountAddsBook);
                            m_CheckoutAddressAddEdit.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, m_CheckoutAddressAddEdit, "m_CheckoutAddressAddEdit");
                            mFT.addToBackStack("m_CheckoutAddressAddEdit");
                            mFT.commit();
                        }
                        //*********************************************************************************

                    }


                }
            });

            holder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (getActivity() != null) {
                        // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder
                                .setMessage(getActivity().getString(R.string.do_you_want_remove_this_address))
                                .setCancelable(true)
                                .setPositiveButton(getActivity().getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        callAddressDeleteAPi(mAddressesList.get(position).getAddress_id());
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                }
                        );

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }


                }
            });


        }


        @Override
        public int getItemCount() {

            return this.mAddressesList.size();

        }

        public class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView mAddress, mAddressSubDetails, mAddressSubDetailsMob, m__Name;
            private TextView mLable;
            private LinearLayout mParentRow;
            private LinearLayout mEdit, mDelete;
            // private SwipeLayout s_Holder;


            public DataObjectHolder(View view) {
                super(view);

                //  s_Holder = itemView.findViewById(R.id.lay_adds_list_row);

                mParentRow = view.findViewById(R.id.lay_checkout_address_list_row);
                mAddressSubDetails = view.findViewById(R.id.tv_edit_account_address_sub_details);
                mAddress = view.findViewById(R.id.tv_edit_account_address);
                mAddressSubDetailsMob = view.findViewById(R.id.tv_edit_account_address_sub_details_mobile);
                m__Name = view.findViewById(R.id.tv_edit_account_address_name);


                mEdit = view.findViewById(R.id.img_btn_adds_list_selection_edit);
                mDelete = view.findViewById(R.id.img_btn_adds_list_selection_delete);

                mEdit = itemView.findViewById(R.id.img_btn_adds_list_selection_edit);
                // mEdit.setOnClickListener(this);
                //  mDelete = itemView.findViewById(R.id.img_btn_adds_list_selection_remove);
                // mDelete.setOnClickListener(this);


            }

            @Override
            public void onClick(View view) {

                int mId = view.getId();


            }
        }


    }

    private void showAddressEmptyMsg() {
        mAddressListEmptyMsg.setVisibility(View.VISIBLE);
        mAddsList.setVisibility(View.GONE);
    }

    private void callAddressListApi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {

                    JSONObject jsonObject = new JSONObject();
                    try {

                        String mCustomerAuthorization = "";
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                        }

                        jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                        jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                        mProgressDialog.show();
                        retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                        Call<AddressListApi> Call = retrofitInterface.addressListsApi(mCustomerAuthorization,body);
                        Call.enqueue(new Callback<AddressListApi>() {
                            @Override
                            public void onResponse(@NonNull Call<AddressListApi> call, @NonNull Response<AddressListApi> response) {

                                mProgressDialog.cancel();

                                if (getActivity() != null) {

                                    if (response.isSuccessful()) {
                                        mAddressListApi = response.body();

                                        if (mAddressListApi != null) {
                                            //Log.e("mAddressListApi", "not null");
                                            if (mAddressListApi.success != null) {
                                                //Api response successDataSet :-

                                                if (mAddressListApi.addressList != null && mAddressListApi.addressList.size() > 0) {

                                                    mAddressListEmptyMsg.setVisibility(View.GONE);
                                                    mAddsList.setVisibility(View.VISIBLE);

                                                    mAddsListLayoutMgr = new LinearLayoutManager(getActivity());
                                                    mAddsList.setLayoutManager(mAddsListLayoutMgr);
                                                    myAccountAddressListAdapter = new MyAccountAddressListAdapter(mAddressListApi.addressList);
                                                    mAddsList.setAdapter(myAccountAddressListAdapter);

                                                } else {
                                                    showAddressEmptyMsg();
                                                }


                                            } else {

                                                showAddressEmptyMsg();

                                                mProgressDialog.cancel();

                                                //Api response failure :-
                                                if (mAddressListApi.error != null) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mAddressListApi.error.message);
                                                }
                                            }
                                        } else {

                                            showAddressEmptyMsg();

                                            mProgressDialog.cancel();

                                            //Log.e("mAddressListApi", "null");
                                        }
                                    } else {

                                        showAddressEmptyMsg();

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
                            public void onFailure(@NonNull Call<AddressListApi> call, @NonNull Throwable t) {
                                mProgressDialog.cancel();
                                showAddressEmptyMsg();

                            }
                        });

                    } catch (Exception e) {

                        showAddressEmptyMsg();

                        mProgressDialog.cancel();

                        //Log.e("210 Excep ", e.toString());
                        e.printStackTrace();

                    }

                } else {
                    mProgressDialog.cancel();
                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);

                }

            } else {

                if (mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                        //Its - my account page address book call.
                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }

                }

            }
        }


    }

    private void callIsDeliveryAPi(String latitude, String longitude, String addressID, String zoneID) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {

                    JSONObject jsonObject = new JSONObject();
                    try {


                        AddressGeocodeDataSet mAddressGDs = RestaurantAddressDB.getInstance(getActivity()).getGeocodeDetails();

                        jsonObject.put(DefaultNames.vendor_id, mAddressGDs.getRestaurantId());
                        jsonObject.put(DefaultNames.latitude, latitude);
                        jsonObject.put(DefaultNames.longitude, longitude);
                        jsonObject.put(DefaultNames.address_id, addressID);
                        jsonObject.put(DefaultNames.zone_id, zoneID);
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            jsonObject.put(DefaultNames.guest_status, "0");
                            jsonObject.put(DefaultNames.guest_id, "");
                        } else {
                            //safe post :-
                            jsonObject.put(DefaultNames.guest_status, "1");
                            jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                        }
                        jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                        jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                        String mCustomerAuthorization = "";
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                        }
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                        retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                        Call<ApiResponseCheck> Call = retrofitInterface.isDeliveryApi(mCustomerAuthorization, body);
                        mProgressDialog.show();
                        Call.enqueue(new Callback<ApiResponseCheck>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {
                                mProgressDialog.cancel();


                                if (response.isSuccessful()) {

                                    ApiResponseCheck mApiResponseCheck = response.body();

                                    if (mApiResponseCheck != null) {

                                        if (mApiResponseCheck.success != null) {
                                            //Api response successDataSet :-
                                            if (getActivity() != null) {
                                                getParentFragmentManager().popBackStack();
                                            }
                                        } else {
                                            //Api response failure :-

                                            if (getActivity() != null) {
                                                if (mApiResponseCheck.error != null) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
                                                }
                                            }
                                        }

                                    }

                                } else {

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
                            public void onFailure(@NonNull Call<ApiResponseCheck> call, @NonNull Throwable t) {
                                mProgressDialog.cancel();
                            }
                        });

                    } catch (JSONException e) {
                        mProgressDialog.cancel();

                        //Log.e("210 Excep ", e.toString());
                        e.printStackTrace();

                    }

                } else {

                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);

                }

            } else {
                if (mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                        //Its - my account page address book call.
                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }

                }
            }
        }


    }

    private void callAddressDeleteAPi(String addressId) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {

                    JSONObject jsonObject = new JSONObject();
                    try {

                        jsonObject.put(DefaultNames.address_id, addressId);

                        jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                        jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                        String mCustomerAuthorization = "";
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                        }
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                        retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                        Call<ApiResponseCheck> Call = retrofitInterface.addressDeleteApi(mCustomerAuthorization, body);
                        mProgressDialog.show();
                        Call.enqueue(new Callback<ApiResponseCheck>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {

                                if (response.isSuccessful()) {

                                    ApiResponseCheck mApiResponseCheck = response.body();

                                    if (mApiResponseCheck != null) {


                                        if (mApiResponseCheck.success != null) {
                                            //Api response successDataSet :-
                                            AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.success.message);
                                            callAddressListApi();
                                        } else {
                                            //Api response failure :-
                                            mProgressDialog.cancel();
                                            if (getActivity() != null) {
                                                if (mApiResponseCheck.error != null) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
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
                            public void onFailure(@NonNull Call<ApiResponseCheck> call, @NonNull Throwable t) {
                                mProgressDialog.cancel();
                            }
                        });

                    } catch (JSONException e) {
                        mProgressDialog.cancel();

                        //Log.e("210 Excep ", e.toString());
                        e.printStackTrace();

                    }

                } else {

                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);

                }

            } else {
                if (mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                        //Its - my account page address book call.
                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }

                }
            }
        }


    }


}
