package stubs.logic.fof;

import beans.FOFQuickInfo;
import beans.PriceInfo;
import bl.fof.FOFBaseInfoLogic;
import util.SectorType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 2016/8/28.
 */
public class FOFBaseInfoLogicStub extends UnicastRemoteObject implements FOFBaseInfoLogic {
    public FOFBaseInfoLogicStub() throws RemoteException {

    }

    @Override
    public FOFQuickInfo getFOFQuickInfo() throws RemoteException {
        FOFQuickInfo quickInfo = new FOFQuickInfo();
        quickInfo.code = "0001";
        quickInfo.name = "我的FOF";
        quickInfo.establish_date = "2016-08-28";
        quickInfo.netWorth = 2626021561.0;
        quickInfo.performanceBase = "沪深300";
        quickInfo.totalProfit = 20;
        return quickInfo;
    }

    @Override
    public FOFQuickInfo getFOFQuickInfo(String code) throws RemoteException {
        return getFOFQuickInfo();
    }

    @Override
    public Map<String, List<String>> getFundCodeInFOF() throws RemoteException {
        Map<String, List<String>> infos = new HashMap<>();
        infos.put(SectorType.FIX_PROFIT_TYPE, Arrays.asList("002441", "519061", "519060"));
        infos.put(SectorType.RIGHTS_TYPE, Arrays.asList("165525", "512880", "162107", "002588"));
        return infos;
    }

    @Override
    public List<PriceInfo> getFOFPriceInfos() throws RemoteException {
        PriceInfo info = new PriceInfo();
        info.date = "2016-08-25";
        info.price = 1.5;
        info.total_netWorth = 3.5;
        info.rise = 0.04;
        PriceInfo info1 = new PriceInfo();
        info1.date = "2016-08-26";
        info1.price = 1.6;
        info1.total_netWorth = 3.6;
        info1.rise = 5.65;
        return Arrays.asList(info, info1);
    }
}