package me.laotang.router.result;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ActivityResult implements Parcelable {
    private final int mResultCode;
    @Nullable
    private final Intent mData;

    /**
     * Create a new instance
     *
     * @param resultCode status to indicate the success of the operation
     * @param data an intent that carries the result data
     */
    public ActivityResult(int resultCode, @Nullable Intent data) {
        mResultCode = resultCode;
        mData = data;
    }

    ActivityResult(Parcel in) {
        mResultCode = in.readInt();
        mData = in.readInt() == 0 ? null : Intent.CREATOR.createFromParcel(in);
    }

    /**
     * @return the resultCode
     */
    public int getResultCode() {
        return mResultCode;
    }

    /**
     * @return the intent
     */
    @Nullable
    public Intent getData() {
        return mData;
    }

    @Override
    public String toString() {
        return "ActivityResult{"
                + "resultCode=" + resultCodeToString(mResultCode)
                + ", data=" + mData
                + '}';
    }

    /**
     * A readable representation of standard activity result codes
     *
     * @param resultCode the result code
     *
     * @return RESULT_OK, RESULT_CANCELED, or the number otherwise
     */
    @NonNull
    public static String resultCodeToString(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK: return "RESULT_OK";
            case Activity.RESULT_CANCELED: return "RESULT_CANCELED";
            default: return String.valueOf(resultCode);
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mResultCode);
        dest.writeInt(mData == null ? 0 : 1);
        if (mData != null) {
            mData.writeToParcel(dest, flags);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Creator<ActivityResult> CREATOR = new Creator<ActivityResult>() {
        @Override
        public ActivityResult createFromParcel(@NonNull Parcel in) {
            return new ActivityResult(in);
        }

        @Override
        public ActivityResult[] newArray(int size) {
            return new ActivityResult[size];
        }
    };
}
