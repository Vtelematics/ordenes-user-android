package com.ordenese.ApiConnection;

import com.ordenese.DataSets.AddressListApi;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.CheckoutCartListApi;
import com.ordenese.DataSets.CommonFunctionOtpDataSet;
import com.ordenese.DataSets.ConfirmOrderApi;
import com.ordenese.DataSets.CountryCodeApi;
import com.ordenese.DataSets.CouponApplyApi;
import com.ordenese.DataSets.CouponListApi;
import com.ordenese.DataSets.CuisinesApi;
import com.ordenese.DataSets.FilterListApi;
import com.ordenese.DataSets.GPApiResponseCheck;
import com.ordenese.DataSets.GPLApiResponseCheck;
import com.ordenese.DataSets.GroceryInfoApi;
import com.ordenese.DataSets.GroceryProductApi;
import com.ordenese.DataSets.GroceryProductInfo;
import com.ordenese.DataSets.GroceryProducts;
import com.ordenese.DataSets.GroceryStoresListApi;
import com.ordenese.DataSets.GroceryStoresSearchApi;
import com.ordenese.DataSets.HomeModulesApi;
import com.ordenese.DataSets.LoginApi;
import com.ordenese.DataSets.MyOrderInfoApi;
import com.ordenese.DataSets.MyOrderListApi;
import com.ordenese.DataSets.OtpSuccess;
import com.ordenese.DataSets.PagesApi;
import com.ordenese.DataSets.PaymentMethodsApi;
import com.ordenese.DataSets.ProfilePictureApi;
import com.ordenese.DataSets.RegisterApi;
import com.ordenese.DataSets.SubCategoryDataSet;
import com.ordenese.DataSets.Success;
import com.ordenese.DataSets.TrackOrderApi;
import com.ordenese.DataSets.VendorDataSet;
import com.ordenese.DataSets.VendorFilterApi;
import com.ordenese.DataSets.VendorListingApi;
import com.ordenese.DataSets.VendorListingApiPagination;
import com.ordenese.DataSets.VendorSearchApi;
import com.ordenese.DataSets.VendorZoneApi;
import com.ordenese.DataSets.Vendor_Info;
import com.ordenese.DataSets.CartListApi;
import com.ordenese.DataSets.ReviewListApi;
import com.ordenese.DataSets.WishListDataSet;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RetrofitInterface {

    @POST("api/ordering/add-cart")
    Call<String> add_to_cart(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/cart-product-count")
    Call<String> cart_product_count(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/cart-delete")
    Call<String> clear_cart(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/vendor-info")
    Call<Vendor_Info> vendorInfo(@Body RequestBody jsonObject);

    @POST("api/ordering/modules")
    Call<HomeModulesApi> homeModules(@Body RequestBody jsonObject);

    @POST("api/ordering/modules")
    Call<ApiResponseCheck> homeModulesForDeliveryAvailableCheck(@Body RequestBody jsonObject);

    @POST("api/ordering/vendor-listing")
    Call<VendorListingApi> AllRestaurantsList(@Body RequestBody jsonObject);

    @POST("api/ordering/vendor-listing")
    Call<VendorListingApiPagination> AllRestaurantsListPagination(@Body RequestBody jsonObject);

    @POST("api/ordering/cuisine-list")
    Call<CuisinesApi> CuisinesList(@Body RequestBody jsonObject);

    @POST("api/ordering/vendor-search")
    Call<VendorSearchApi> searchRestaurantsList(@Body RequestBody jsonObject);

    @POST("api/ordering/country-list")
    Call<CountryCodeApi> countryCodeList(@Body RequestBody jsonObject);

    @POST("api/ordering/login")
    Call<LoginApi> loginApi(@Body RequestBody jsonObject);

    @POST("api/ordering/profile-info")
    Call<LoginApi> profile_info(@Header("Customer-Authorization") String authorization);

    @POST("api/ordering/check-customer")
    Call<ApiResponseCheck> checkCustomerApi(@Body RequestBody jsonObject);

    @POST("api/ordering/api-sendOTP")
    Call<OtpSuccess> sendOTP(@Body RequestBody jsonObject);

    @POST("api/ordering/api-verifyOTP")
    Call<OtpSuccess> verifyOTP(@Body RequestBody jsonObject);
    @POST("api/ordering/common-function")
    Call<CommonFunctionOtpDataSet> commonFunctionOtp(@Body RequestBody jsonObject);

    @POST("api/ordering/forget-password")
    Call<ApiResponseCheck> forgotPwdApi(@Body RequestBody jsonObject);

    @POST("api/ordering/register")
    Call<RegisterApi> registerApi(@Body RequestBody jsonObject);

    @POST("api/ordering/cart-product")
    Call<CartListApi> cartListApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/cart-product")
    Call<CheckoutCartListApi> checkoutCartListApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/option-count-change")
    Call<GPApiResponseCheck> groceryCartItemIncrementOrDecrementApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/option-count-change")
    Call<GPLApiResponseCheck> groceryPLCartItemIncrementOrDecrementApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/option-count-change")
    Call<ApiResponseCheck> cartItemIncrementOrDecrementApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/cart-delete")
    Call<GPApiResponseCheck> groceryCartItemDeleteApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/cart-delete")
    Call<GPLApiResponseCheck> groceryPLCartItemDeleteApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/cart-delete")
    Call<ApiResponseCheck> cartItemDeleteApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/coupon-list")
    Call<CouponListApi> couponListApi(@Header("Customer-Authorization") String authorization,@Body RequestBody jsonObject);

    @POST("api/ordering/payment-list")
    Call<PaymentMethodsApi> paymentMethodsApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/apply-coupon")
    Call<CouponApplyApi> applyCouponApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/confirm-order")
    Call<ConfirmOrderApi> confirmOrderApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/place-order")
    Call<ApiResponseCheck> placeOrderApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/address-list")
    Call<AddressListApi> addressListsApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/is-delivery")
    Call<ApiResponseCheck> isDeliveryApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/delete-address")
    Call<ApiResponseCheck> addressDeleteApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/add-address")
    Call<ApiResponseCheck> addAddressApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/edit-address")
    Call<ApiResponseCheck> editAddressApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/pages")
    Call<PagesApi> webViewPagesApi(/*@Header("Customer-Authorization") String authorization,*/ @Body RequestBody jsonObject);

    @POST("api/ordering/contactUs")
    Call<ApiResponseCheck> contactUsApi(@Body RequestBody jsonObject);

    @POST("api/ordering/change-password")
    Call<ApiResponseCheck> changePasswordApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/order-list")
    Call<MyOrderListApi> ordersListsApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/order-info")
    Call<MyOrderInfoApi> orderInfoApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/edit-profile")
    Call<ApiResponseCheck> editProfileApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/profile-picture")
    Call<ProfilePictureApi> changeProfilePictureApi(@Header("Customer-Authorization") String Token, @Body RequestBody jsonObject);

    @POST("api/ordering/Vendor-zone")
    Call<VendorZoneApi> vendorZoneApi(@Body RequestBody jsonObject);

    @POST("api/ordering/vendor-listing")
    Call<GroceryStoresListApi> groceryStoresList(@Body RequestBody jsonObject);

    @POST("api/ordering/vendor-info")
    Call<GroceryInfoApi> groceryInfo(@Body RequestBody jsonObject);

    @POST("api/ordering/grocery/product")
    Call<GroceryProductApi> groceryProductList(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/grocery/subcategory")
    Call<SubCategoryDataSet> subcategory(@Body RequestBody jsonObject);

    @POST("api/ordering/grocery/product")
    Call<GroceryProducts> product(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/grocery/product-info")
    Call<GroceryProductInfo> product_info(@Body RequestBody jsonObject);

    @POST("api/ordering/vendor/filter")
    Call<VendorFilterApi> vendorFiltersApi(@Body RequestBody jsonObject);

    @POST("api/ordering/guest-address")
    Call<ApiResponseCheck> guestAddEditAddressApi(@Body RequestBody jsonObject);

    @POST("api/ordering/review-list")
    Call<ReviewListApi> reviewListApi(@Body RequestBody jsonObject);

    @POST("api/ordering/add-review")
    Call<ApiResponseCheck> addReviewApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/grocery/grocery-search")
    Call<GroceryStoresSearchApi> grocerySearchApi(@Body RequestBody jsonObject);

    @POST("api/ordering/filter-list")
    Call<FilterListApi> filterListApi(@Body RequestBody jsonObject);

    @POST("api/ordering/track-order")
    Call<TrackOrderApi> trackOrderApi(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/delete-account")
    Call<ApiResponseCheck> customer_account_deletion(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/wish-list")
    Call<WishListDataSet> wishlist(@Body RequestBody jsonObject);

    @POST("api/ordering/order-cancel")
    Call<String> cancel_order(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);

    @POST("api/ordering/logout")
    Call<String> log_out(@Header("Customer-Authorization") String authorization, @Body RequestBody jsonObject);


}
