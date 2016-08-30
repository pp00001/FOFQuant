package strategyimpl;

import beans.FundQuickInfo;
import beans.PriceInfo;
import bl.BaseInfoLogic;
import bl.MarketLogic;
import blimpl.BLController;
import dataservice.BaseInfoDataService;
import dataserviceimpl.DataServiceController;
import entities.FundInfosEntity;
import entities.FundRankEntity;
import exception.ObjectNotFoundException;
import exception.ParameterException;
import strategy.FundDeployStrategy;
import util.SectorType;
import util.UnitType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by Seven on 16/8/30.
 */
public class FundDeployStrategyImpl implements FundDeployStrategy {
    private BaseInfoDataService baseInfoDataService;
    private BaseInfoLogic baseInfoLogic;
    private MarketLogic marketLogic;
    private String startDate = "2013-12-31";
    private String endDate = "2015-12-31";

    public FundDeployStrategyImpl() {
        baseInfoDataService = DataServiceController.getBaseInfoDataService();
        baseInfoLogic = BLController.getBaseInfoLogic();
        marketLogic = BLController.getMarketLogic();
    }


    @Override
    public List getWRpturn(List<String> funds, String startDate, String endDate) {
        return null;
    }


    @Override
    public List DefaultFundDeploy() throws RemoteException {
        //获得系统中所有固定收益类基金的排名
        List<FundQuickInfo> fundQuickInfos = null;
        try {
            fundQuickInfos = baseInfoLogic.getFundQuickInfo(SectorType.FIX_PROFIT_TYPE);
            HashMap<String, Double> fixProfitRank = new HashMap<>();
            FundRankEntity fundRankEntity;
            for (FundQuickInfo fundQuickInfo : fundQuickInfos) {
                String code = "";
                try {
                    code = fundQuickInfo.code;
                    fundRankEntity = baseInfoDataService.getFundRankInfo(code);
                    Double rank = fundRankEntity.getRank();
                    if (rank != null) {
                        fixProfitRank.put(code, rank);
                    }
                } catch (ObjectNotFoundException e) {
                    System.out.println(code + "no rank info");
                }
            }

            int[] windows = {90, 180, 360};
            int[] holds = {30, 60, 90};
            int N;
            for (int window : windows) {
                for (int hold : holds) {
                    for (N = 2; N < 6; N++) {
                        this.AdjustiveFundDeploy(fixProfitRank, N, window, hold);
                    }
                }
            }
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List CustomizedFundDeploy(List<String> funds) {
        return null;
    }

    @Override
    public void AdjustiveFundDeploy(Map<String, Double> codeRank, int N, int window, int hold) throws RemoteException {
        //排序
        List<String> sortedCodes = this.sort(codeRank);

        //读取系统中前N只固定收益型股票的2013.12-2015.12收盘价数据,每只基金对应的费率
        //N=2,可调
        if (sortedCodes.size() < N) {
            N = sortedCodes.size();
        }

        Map<String, List<Double>> codePrices = this.getCodePrices(sortedCodes, N);
        Map<String, List<Double>> codeFee = this.getCodeFee(sortedCodes, N);

        this.writeToTXT(codePrices,codeFee,N,window,hold);

    }

    public Map<String, List<Double>> getCodePrices(List<String> funds, int N) throws RemoteException {
        Map<String, List<Double>> codePrices = new HashMap<>();
        int size = 0;
        try {
            size = marketLogic.getPriceInfo(funds.get(0), UnitType.DAY, startDate, endDate).size();
            int i = 0;
            while (i < N) {
                String code = funds.get(i);
                List<PriceInfo> priceInfos = marketLogic.getPriceInfo(code, UnitType.DAY, startDate, endDate);
                if (priceInfos.size() < size) {
                    size = priceInfos.size();
                }
                //<基金,收盘价序列>
                List<Double> prices = new ArrayList<>();
                for (int j = 0; j < priceInfos.size(); j++) {
                    prices.add(priceInfos.get(j).price);
                }
                codePrices.put(code, prices);
            }
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        } catch (ParameterException e) {
            e.printStackTrace();
        }
        return codePrices;
    }

    public Map<String, List<Double>> getCodeFee(List<String> funds, int N) throws RemoteException {
        Map<String, List<Double>> codeFee = new HashMap<>();
        int size = 0;
        try {
            size = marketLogic.getPriceInfo(funds.get(0), UnitType.DAY, startDate, endDate).size();
            int i = 0;
            while (i < N) {
                String code = funds.get(i);
                List<PriceInfo> priceInfos = marketLogic.getPriceInfo(code, UnitType.DAY, startDate, endDate);
                if (priceInfos.size() < size) {
                    size = priceInfos.size();
                }

                //<基金,费率序列>
                List<Double> fees = new ArrayList<>();
                FundInfosEntity fundInfos = baseInfoDataService.getFundInfo(code);
                Double manageFee = fundInfos.getManageFee();
                Double trusteeFee = fundInfos.getTrusteeFee();
                Double saleServiceFee = fundInfos.getSaleServiceFee();
                if (manageFee != null) {
                    fees.add(manageFee);
                } else {
                    fees.add(0.0);
                }
                if (trusteeFee != null) {
                    fees.add(trusteeFee);
                } else {
                    fees.add(0.0);
                }
                if (saleServiceFee != null) {
                    fees.add(saleServiceFee);
                } else {
                    fees.add(0.0);
                }
                codeFee.put(code, fees);

                i++;
            }
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        } catch (ParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void writeToTXT(Map<String,List<Double>> codePrices,Map<String, List<Double>> codeFee,int N,int window,int hold) {
        String name1 = "riskyParityN" + N + "W" + window + "H" + hold + "Price";
        String name2 = "riskyParityN" + N + "W" + window + "H" + hold + "Fee";
        //将收盘价数据写入txt文件
        File priceFile = new File(name1);
        File feeFile = new File(name2);
        try {
            if (!priceFile.exists()) {
                priceFile.createNewFile();
            }
            if (!feeFile.exists()) {
                feeFile.createNewFile();
            }

            FileWriter priceFileWriter = new FileWriter(priceFile.getName());
            BufferedWriter bufferedWriter = new BufferedWriter(priceFileWriter);

            FileWriter feeFileWriter = new FileWriter(feeFile.getName());
            BufferedWriter bufferedWriter1 = new BufferedWriter(feeFileWriter);

            for (int day = 0; day < codeFee.get(0).size(); day++) {
                for (String fundcode : codePrices.keySet()) {
                    double close = codePrices.get(fundcode).get(day);
                    bufferedWriter.write(close + " ");
                    if (day == 0) {
                        for (int i = 0; i < 3; i++) {
                            double fee = codeFee.get(fundcode).get(i);
                            bufferedWriter1.write(fee + " ");
                        }
                        bufferedWriter1.write("\n");
                    }
                }
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
            bufferedWriter1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //排序
    private List<String> sort(Map<String,Double> index){
        List<Map.Entry<String,Double>> fundCodes=new ArrayList<>(index.entrySet());
        //按照升序排序
        Collections.sort(fundCodes, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
//                if(o2.getValue()>o1.getValue()){
//                    return -1;
//                }else if (o2.getValue()==o1.getValue()){
//                    return 0;
//                }else{
//                    return 1;
//                }
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        List<String> codesAfterSort=new ArrayList<>();
        for(int i=0;i<fundCodes.size();i++) {
            codesAfterSort.add(fundCodes.get(i).getKey());
        }
        return codesAfterSort;
    }

}
