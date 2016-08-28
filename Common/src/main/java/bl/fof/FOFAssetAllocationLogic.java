package bl.fof;

import beans.AssetItem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 2016/8/26.
 */
public interface FOFAssetAllocationLogic extends Remote {
    /**
     * 获得支持的所有的日期
     * @return
     * @throws RemoteException
     */
    List<String> getAllSupportDate() throws RemoteException;
    /**
     * 设置日期
     * @param date
     */
    void setDate(String date)throws RemoteException;
    /**
     * 获得资产配置情况
     * @return
     */
    List<AssetItem> getFOFAssetAllocation()throws RemoteException;
    /**
     * 获得大类配置权重情况
     * @return
     */
    Map<String,Double> getWeights()throws RemoteException;
}
