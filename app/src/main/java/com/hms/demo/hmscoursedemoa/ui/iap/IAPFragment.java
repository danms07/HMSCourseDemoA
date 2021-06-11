package com.hms.demo.hmscoursedemoa.ui.iap;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hms.demo.hmscoursedemoa.R;
import com.hms.demo.hmscoursedemoa.databinding.IapBinding;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.iap.util.IapClientHelper;
import com.huawei.hms.support.api.client.Status;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class IAPFragment extends Fragment implements ProductAdapter.ProductItemListener {
    private ProductAdapter adapter;
    private IapBinding binding;
    private static final int RESOLUTION_CODE = 100;
    private static final int CONSUMABLE_PURCHASE_CODE = 200;

    public IAPFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ProductAdapter(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESOLUTION_CODE: {
                if (data != null) {
                    // Call the parseRespCodeFromIntent method to obtain the result of the API request.
                    int returnCode = IapClientHelper.parseRespCodeFromIntent(data);
                    // Call the parseAccountFlagFromIntent method to obtain the account type returned by the API.
                    int accountFlag = IapClientHelper.parseAccountFlagFromIntent(data);
                    if (returnCode == 0) {
                        obtainProducts();
                    }

                }
                break;
            }

            case CONSUMABLE_PURCHASE_CODE: {
                handlePurchaseResult(data);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = IapBinding.inflate(inflater, container, false);
        binding.recyclerView.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Task<IsEnvReadyResult> task = Iap.getIapClient(requireActivity()).isEnvReady();
        task.addOnSuccessListener((result) -> {
            int accountFlag = result.getAccountFlag();
            if (result.getReturnCode() == 0) {
                obtainProducts();
            }
        });

        task.addOnFailureListener((failure) -> {
            if (failure instanceof IapApiException) {
                IapApiException apiException = (IapApiException) failure;
                Status status = apiException.getStatus();
                if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                    // HUAWEI ID is not signed in.
                    if (status.hasResolution()) {
                        try {
                            // 6666 is a constant defined by yourself.
                            // Open the sign-in screen returned.
                            status.startResolutionForResult(requireActivity(), RESOLUTION_CODE);
                        } catch (IntentSender.SendIntentException exp) {
                            Log.e("IAP", exp.toString());
                        }
                    }
                } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                    // The current location does not support HUAWEI IAP.
                }
            } else {
                // Other external errors.
            }
        });
    }

    public void obtainProducts() {
        List<String> productIdList = new ArrayList<>();
// Only those products already configured in AppGallery Connect can be queried.
        //productIdList.add("IAPDEMO");
        //productIdList.add("CONSUMABLE_1");
        productIdList.add("Producto1");
        ProductInfoReq req = new ProductInfoReq();
        // priceType: 0: consumable; 1: non-consumable; 2: subscription
        req.setPriceType(1);
        req.setProductIds(productIdList);
        // Obtain the Activity object that calls the API.
        Activity activity = requireActivity();
        // Call the obtainProductInfo API to obtain the details of the product configured in AppGallery Connect.
        Task<ProductInfoResult> task = Iap.getIapClient(activity).obtainProductInfo(req);
        task.addOnSuccessListener((result) -> {
                    // Obtain details of the product if the request is successful.
                    List<ProductInfo> productList = result.getProductInfoList();
                    adapter.setProducts(productList);
                    adapter.notifyDataSetChanged();
                }
        ).addOnFailureListener((e) -> {
                    if (e instanceof IapApiException) {
                        IapApiException apiException = (IapApiException) e;
                        Log.e("IAP", e.toString());
                        int returnCode = apiException.getStatusCode();
                    } else {
                        // Other external errors.
                    }
                }
        );
    }

    @Override
    public void onItemClick(ProductInfo item) {
        Toast.makeText(requireContext(), "Selected:" + item.getProductName(), Toast.LENGTH_SHORT).show();
        performPurchase(item);
    }

    private void performPurchase(ProductInfo item) {
        // Construct a PurchaseIntentReq object.
        PurchaseIntentReq req = new PurchaseIntentReq();
        // Only those products already configured in AppGallery Connect can be purchased through the createPurchaseIntent API.
        req.setProductId(item.getProductId());
        // priceType: 0: consumable; 1: non-consumable; 2: subscription
        req.setPriceType(item.getPriceType());
        req.setDeveloperPayload("test");
        // Obtain the Activity object that calls the API.
        final Activity activity = requireActivity();
        // Call the createPurchaseIntent API to create a managed product order.
        Task<PurchaseIntentResult> task = Iap.getIapClient(activity).createPurchaseIntent(req);
        task.addOnSuccessListener((result) -> {
                    // Obtain the order creation result.
                    Status status = result.getStatus();
                    if (status.hasResolution()) {
                        try {
                            // 6666 is a constant defined by yourself.
                            // Open the checkout screen returned.
                            status.startResolutionForResult(activity, CONSUMABLE_PURCHASE_CODE);
                        } catch (IntentSender.SendIntentException exp) {
                        }
                    }
                }
        ).addOnFailureListener((e) -> {
                    if (e instanceof IapApiException) {
                        IapApiException apiException = (IapApiException) e;
                        Status status = apiException.getStatus();
                        int returnCode = apiException.getStatusCode();
                    } else {
                        // Other external errors.
                    }
                }
        );
    }

    private void handlePurchaseResult(Intent data) {
        PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(requireActivity()).parsePurchaseResultInfoFromIntent(data);
        switch (purchaseResultInfo.getReturnCode()) {
            case OrderStatusCode.ORDER_STATE_CANCEL:
                // The user cancels the purchase.
                break;
            case OrderStatusCode.ORDER_STATE_FAILED:
            case OrderStatusCode.ORDER_PRODUCT_OWNED:
                // Check whether the delivery is successful.
                retryDelivery();
                break;
            case OrderStatusCode.ORDER_STATE_SUCCESS:
                // The payment is successful.
                String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();
                // Verify the signature using your app's IAP public key.
                // Start delivery if the verification is successful.
                // Call the consumeOwnedPurchase API to consume the product after delivery if the product is a consumable.
                try {

                    InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);


                    ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
                    String productId=inAppPurchaseDataBean.getProductId();

                    String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                    //Realizar peticion web
                    req.setPurchaseToken(purchaseToken);
                    provideProduct(productId);
                    consumePurchase(productId,req);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void retryDelivery() {
        OwnedPurchasesReq ownedPurchasesReq=new OwnedPurchasesReq();
        ownedPurchasesReq.setPriceType(0);
        Task<OwnedPurchasesResult> task=Iap.getIapClient(requireActivity()).obtainOwnedPurchases(ownedPurchasesReq);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                // Obtain the execution result if the request is successful.
                if (result != null && result.getInAppPurchaseDataList() != null) {
                    for (int i = 0; i < result.getInAppPurchaseDataList().size(); i++) {
                        String inAppPurchaseData = result.getInAppPurchaseDataList().get(i);
                        String inAppSignature = result.getInAppSignature().get(i);
                        // Use the IAP public key to verify the signature of inAppPurchaseData.
                        // Check the purchase status of each product if the verification is successful. When the payment has been made, deliver the required product. After the delivery is performed, consume the product.
                        try {
                            InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
                            int purchaseState = inAppPurchaseDataBean.getPurchaseState();
                            if(purchaseState==0){
                                provideProduct(inAppPurchaseDataBean.getProductId());
                                String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                                ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
                                req.setPurchaseToken(purchaseToken);
                                consumePurchase(inAppPurchaseDataBean.getProductId(),req);
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
            }
        }).addOnFailureListener((e) -> {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                } else {
                    // Other external errors.
                }
            }
        );
    }

    private void provideProduct(String productId) {
        //TODO aumentar vidas, gemas, monedas o el producto que el usuario haya adquirido
        Toast.makeText(requireContext(),"El producto: $productId se adquirió exitosamente",Toast.LENGTH_LONG).show();
    }

    private void consumePurchase(String productId,ConsumeOwnedPurchaseReq req) {
        //Do something with the confirmed productId
        Toast.makeText(requireContext(),"Payment Successful ProductId:"+productId,Toast.LENGTH_LONG).show();
        // Call the consumeOwnedPurchase API to consume the product after delivery if the product is a consumable.
        Task<ConsumeOwnedPurchaseResult> task = Iap.getIapClient(requireActivity()).consumeOwnedPurchase(req);
        task.addOnSuccessListener((result) ->{
                // Obtain the execution result.
            }
        ).addOnFailureListener((Exception e) ->{
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                } else {
                    // Other external errors.
                }
            }
        );
    }
}