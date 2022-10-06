package com.hms.demo.hmscoursedemoa.ui.iap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hms.demo.hmscoursedemoa.R;
import com.hms.demo.hmscoursedemoa.databinding.IapBinding;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
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
    private static final int RESOLUTION_CODE = 100;
    private static final int CONSUMABLE_PURCHASE_CODE = 200;
    private int priceType=0;

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
                    //int accountFlag = IapClientHelper.parseAccountFlagFromIntent(data);
                    if (returnCode == 0) {
                        obtainProducts(priceType);
                    }

                }
                break;
            }

            case CONSUMABLE_PURCHASE_CODE: {
                handlePurchaseResult(data);
                break;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        com.hms.demo.hmscoursedemoa.databinding.IapBinding binding = IapBinding.inflate(inflater, container, false);
        binding.recyclerView.setAdapter(adapter);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.purchase_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.optionConsumable) {
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE;
        }
        else if(item.getItemId()==R.id.optionNonConsumable){
                priceType= IapClient.PriceType.IN_APP_NONCONSUMABLE;
        }
        obtainProducts(priceType);
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void obtainProducts(int priceType) {
        List<String> productIdList = loadProductsByType(priceType);

        ProductInfoReq req = new ProductInfoReq();
        // priceType: 0: consumable; 1: non-consumable; 2: subscription
        req.setPriceType(priceType);
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
                        Log.e("IAPException", "Obtain products task failed with code:"+apiException.getStatusCode());

                    } else {
                        // Other external errors.
                        Log.e("IAPException", e.toString());
                    }
                }
        );
    }

    private List<String> loadProductsByType(int priceType) {
        ArrayList<String> products=new ArrayList<>();
        // Only those products already configured in AppGallery Connect can be queried.
        if(priceType == IapClient.PriceType.IN_APP_CONSUMABLE) {//Fill the list with consumables
            products.add("CONSUMABLE_1");
        }
        else if(priceType== IapClient.PriceType.IN_APP_NONCONSUMABLE){
            products.add("Producto1");
        }
        return products;
    }

    @Override
    public void onItemClick(ProductInfo item) {
        Toast.makeText(requireContext(), "Selected:" + item.getProductName(), Toast.LENGTH_SHORT).show();
        performPurchase(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Task<IsEnvReadyResult> task = Iap.getIapClient(requireActivity()).isEnvReady();
        task.addOnSuccessListener((result) -> {
            if (result.getReturnCode() == 0) {
                checkPreviousPurchases();
                obtainProducts(priceType);
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
                            // Open the sign-in screen returned.
                            status.startResolutionForResult(requireActivity(), RESOLUTION_CODE);
                        } catch (IntentSender.SendIntentException exp) {
                            Log.e("IAP", exp.toString());
                        }
                    }
                } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                    // The current location does not support HUAWEI IAP.
                    Toast.makeText(requireContext(),R.string.invalidIAPCountry,Toast.LENGTH_SHORT).show();
                }
            } else {
                // Other external errors.
                Toast.makeText(requireContext(),R.string.unknownError,Toast.LENGTH_SHORT).show();
            }
        });
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
                            Log.e("IAPException"," "+ exp.getMessage());
                        }
                    }
                }
        ).addOnFailureListener((e) -> {
                    if (e instanceof IapApiException) {
                        IapApiException apiException = (IapApiException) e;
                        Status status = apiException.getStatus();
                        int returnCode = apiException.getStatusCode();
                        Log.e("IAPException","Task failed with code:"+returnCode+"\tStatus:" +status);
                    } else {
                        // Other external errors.
                        Toast.makeText(requireContext(),R.string.unknownError,Toast.LENGTH_SHORT).show();
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
                checkPreviousPurchases();
                break;
            case OrderStatusCode.ORDER_STATE_SUCCESS:
                // The payment is successful.
                String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                //String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();
                // Verify the signature using your app's IAP public key.
                // Start delivery if the verification is successful.
                // Call the consumeOwnedPurchase API to consume the product after delivery if the product is a consumable.
                try {

                    InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
                    provideProduct(inAppPurchaseDataBean);

                } catch (JSONException e) {
                    Log.e("IAPException",""+e.getMessage());
                }
                break;
            default:
                break;
        }
    }

    private void checkPreviousPurchases() {
        OwnedPurchasesReq ownedPurchasesReq=new OwnedPurchasesReq();
        ownedPurchasesReq.setPriceType(0);
        Task<OwnedPurchasesResult> task=Iap.getIapClient(requireActivity()).obtainOwnedPurchases(ownedPurchasesReq);
        task.addOnSuccessListener(result -> {
            // Obtain the execution result if the request is successful.
            if (result != null && result.getInAppPurchaseDataList() != null) {
                for (int i = 0; i < result.getInAppPurchaseDataList().size(); i++) {
                    String inAppPurchaseData = result.getInAppPurchaseDataList().get(i);
                    //String inAppSignature = result.getInAppSignature().get(i);
                    // Use the IAP public key to verify the signature of inAppPurchaseData.
                    // Check the purchase status of each product if the verification is successful. When the payment has been made, deliver the required product. After the delivery is performed, consume the product.
                    try {
                        InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
                        int purchaseState = inAppPurchaseDataBean.getPurchaseState();
                        if(purchaseState==0){
                            provideProduct(inAppPurchaseDataBean);

                        }
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(),"Exception retrying the product delivery",Toast.LENGTH_SHORT).show();
                        Log.e("IAP Exception"," "+e.getMessage());
                    }
                }
            }
        }).addOnFailureListener((e) -> {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    Log.e("IAPException","Task failed with code:"+returnCode+"\tStatus:" +status);
                } else {
                    // Other external errors.
                    Log.e("IAPException",getString(R.string.unknownError));
                }
            }
        );
    }

    private void provideProduct(InAppPurchaseData item) {
        //TODO aumentar vidas, gemas, monedas o el producto que el usuario haya adquirido
        Toast.makeText(requireContext(),"El producto: "+item.getProductName() +"se adquirió exitosamente",Toast.LENGTH_LONG).show();
        ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
        req.setPurchaseToken(item.getPurchaseToken());
        consumePurchase(item.getProductId(),req);
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
                    Log.e("IAPException","Status:"+status+"\tReturn code:"+returnCode);
                } else {
                    // Other external errors.
                    Log.e("IAPException",""+e.getMessage());
                }
            }
        );
    }
}