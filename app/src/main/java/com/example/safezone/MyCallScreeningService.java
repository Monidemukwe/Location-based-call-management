package com.example.safezone;

import android.telecom.CallScreeningService;
import android.telecom.Call.Details;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.text.TextUtils;

@RequiresApi(api = Build.VERSION_CODES.P)
public class MyCallScreeningService extends CallScreeningService {
    @Override
    public void onScreenCall(Details callDetails) {
        String incoming = callDetails.getHandle() != null ? callDetails.getHandle().getSchemeSpecificPart() : null;

        boolean allow = true;
        if (!TextUtils.isEmpty(incoming)) {
            allow = SafeZoneManager.getInstance(getApplicationContext()).shouldAllowNumber(incoming);
        }

        CallResponse.Builder resp = new CallResponse.Builder();
        if (allow) {
            resp.setDisallowCall(false).setSkipNotification(false);
        } else {
            resp.setDisallowCall(true).setRejectCall(true).setSkipNotification(true);
        }
        respondToCall(callDetails, resp.build());
    }
}
