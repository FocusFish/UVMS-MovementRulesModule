package eu.europa.ec.fisheries.uvms.movementrules.service.bean;

import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalType;
import eu.europa.ec.fisheries.uvms.movementrules.service.entity.Asset;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class AssetMTEnrichmentResponse implements Serializable {

    private MobileTerminalType mobileTerminalType;
    private Asset asset;
    private List<UUID> assetGroupList = null;

    public AssetMTEnrichmentResponse(){}

    public MobileTerminalType getMobileTerminalType() {
        return mobileTerminalType;
    }

    public void setMobileTerminalType(MobileTerminalType mobileTerminalType) {
        this.mobileTerminalType = mobileTerminalType;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public List<UUID> getAssetGroupList() {
        return assetGroupList;
    }

    public void setAssetGroupList(List<UUID> assetGroupList) {
        this.assetGroupList = assetGroupList;
    }

}
