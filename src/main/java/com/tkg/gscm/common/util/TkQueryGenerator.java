package com.tkg.gscm.common.util;

import com.cello.sclis.framework.util.SessionUtil;
import com.cello.sclis.prp.common.support.PropertyInjectAspectBean;
import com.tkg.gscm.common.db.CommonDao;
import com.tkg.gscm.common.service.CodeService;
import com.tkg.gscm.common.service.CommonService;
import com.tkg.gscm.common.vo.CommonInVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 *  TK QUERY GENERATOR
 * 
 * @System_Name : TK SCM
 * @Author : jong cheol. jeong
 * @Class_Name(en) : TkQueryGenerator
 * @Modification_History : 2018. 06. 04. added for TK
 * 
 * @stereotype Service
 */

@Service("tkQueryGenerator")
public class TkQueryGenerator {
	
	@Inject
	private PropertyInjectAspectBean aspectBean;
	
	@Autowired
	private CodeService codeService;
	
	@Inject
	private CommonService commonService;
	
	@Inject
	private CommonDao dao;
	
	
	private final Log log = LogFactory.getLog(getClass());
	
	static private final String locLevel1ColumnName = "FACTORY_CD";
	static private final String locLevel2ColumnName = "PLANT_CD";
	static private final String locLevel3ColumnName = "LINE_CD";
	
	static private final String modelLevel1ColumnName = "CATEGORY_CD";
	static private final String modelLevel2ColumnName = "CATEGORY_2_CD";
	static private final String modelLevel3ColumnName = "MODEL_ID";
	static private final String modelLevel4ColumnName = "STYLE_CD";
	
	
	//-----------------------------------------------------------------------------------------------------------------------------------------------/
	// Select   | Dimension | _FromH_All    |   LH1.LOC_LV1_ID AS LOC_LV1_ID, LH1.LOC_LV1_NM AS LOC_LV1_NM, LH1.LOC_LV1_ORDB AS LOC_LV1_ORDB
	//          |           |               | , LH1.LOC_LV2_ID AS LOC_LV2_ID, LH1.LOC_LV2_NM AS LOC_LV2_NM, LH1.LOC_LV2_ORDB AS LOC_LV2_ORDB
	//          |           |               | , ...
	//          |           |               | , A.STYLE_NO_ID AS STYLE_NO_ID, A.STYLE_NO_NM, A.STYLE_NO_ORDB
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_All    |   A.LOC_LV1_ID AS LOC_LV1_ID,  A.LOC_LV1_NM AS LOC_LV1_NM,  A.LOC_LV1_ORDB AS LOC_LV1_ORDB
	//          |           |               | , A.LOC_LV2_ID AS LOC_LV2_ID,  A.LOC_LV2_NM AS LOC_LV2_NM,  A.LOC_LV2_ORDB AS LOC_LV2_ORDB
	//          |           |               | , ...
	//          |           |               | , A.STYLE_NO_ID AS STYLE_NO_ID, A.STYLE_NO_NM, A.STYLE_NO_ORDB...
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_Col    |   A.LOC_LV1_ID AS LOC_LV1_ID
	//          |           |               | , A.LOC_LV2_ID AS LOC_LV2_ID
	//          |           |               | , ...
	//          |           |               | , A.STYLE_NO_ID AS STYLE_NO_ID, A.STYLE_NO_NM, A.STYLE_NO_ORDB
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromS_All    |   A.LOC_LV1_ID AS LOC_LV1_ID,  A.LOC_LV1_NM AS LOC_LV1_NM,  A.LOC_LV1_ORDB AS LOC_LV1_ORDB
	//          |           |               | , A.LOC_LV2_ID AS LOC_LV2_ID,  A.LOC_LV2_NM AS LOC_LV2_NM,  A.LOC_LV2_ORDB AS LOC_LV2_ORDB
	//          |           |               | , ...
	//          |           |               | , A.STYLE_NO_ID AS STYLE_NO_ID, A.STYLE_NO_NM, A.STYLE_NO_ORDB...
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromH_All    |   _FromH_All에서 Grouping 제외
	//          |           |   _Nogrouping | 
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_All    |   _FromA_All에서 Grouping 제외
	//          |           |   _Nogrouping | 
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromS_All    |   _FromS_All에서 Grouping 제외
	//          |           |   _Nogrouping | 
	//          |-----------|---------------|--------------------------------------------------------------------------------------------------------/
	//          | Measure   | _FromA        |   A.MEASURE_ID, A.MEASURE_NM, A.MEASURE_SEQ
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromM        |   M.MEASURE_ID, M.MEASURE_NM, M.MEASURE_SEQ
	//          |-----------|---------------|--------------------------------------------------------------------------------------------------------/
	//          | Bucket    | _FromA        |   DECODE(M.MEASURE_ID, 'PLAN_QTY', SUM(DECODE(A.WEEK, 'W201822', A.PLAN_QTY, 0)),
	//          |           |               |                        'PLAN_INIT_QTY', SUM(DECODE(A.WEEK, 'W201822', A.PLAN_INIT_QTY, 0)),
	//          |           |               |   NULL) AS W201822,
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_Stock  |   DECODE(M.MEASURE_ID, 'PLAN_QTY',  SUM(DECODE(A.WEEK, 'W201822', A.PLAN_QTY, 0)),
	//          |           |     OnLastday |                        'STOCK_QTY', SUM(DECODE(A.WEEK_ON_LASTDAY, 'W201822', A.PLAN_INIT_QTY, 0)),
	//          |           |               |   NULL) AS W201822,
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromS        |   SUM(A.W201822) AS W201822
	//          |           |               | , SUM(A.D20180601) AS D20180601
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_Rmain  | , CASE WHEN M.COMP_PLAN_SEQ = '1' THEN CASE WHEN M.MEASURE_ID = 'PLAN_QTY' THEN SUM(A.C1_PLAN_QTY_W201822)
	//          |           |               |                                             WHEN M.MEASURE_ID = 'PROD_QTY' THEN SUM(A.C1_PROD_QTY_W201822)
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_Rsub   | , SUM(DECODE(A.PLAN_ID || A.WEEK, 'TPP_201820_P01' || 'W201822', A.PLAN_QTY, 0)) AS C1_PLAN_QTY_W201822
	//          |           |               | , SUM(DECODE(A.PLAN_ID || A.WEEK, 'TPP_201820_P00' || 'W201822', A.PLAN_QTY, 0)) AS C2_PROD_QTY_W201822
	//          |           |               | , ...
	//----------------------|---------------|--------------------------------------------------------------------------------------------------------/
	// From                 | _HrchyTable   | , CELLOPL.TBL_MST_DMSN_HRCHY_LOC   LH1
	//                      |               | , CELLOPL.TBL_MST_DMSN_HRCHY_MODEL MH1
	//                      |---------------|--------------------------------------------------------------------------------------------------------/
	//                      | _MeasureTable | , ( SELECT 'PLAN_QTY' AS MEASURE_ID, 'Plan Qty' AS MEASURE_NM, '1' AS MEASURE_SEQ FROM DUAL UNION ALL
	//                      |               |     SELECT 'PROD_QTY' AS MEASURE_ID, 'Prod Qty' AS MEASURE_NM, '2' AS MEASURE_SEQ FROM DUAL ...
	//                      |               |   ) M
	//----------------------|---------------|--------------------------------------------------------------------------------------------------------/
	// Where                | _TreeCond     |   AND ('VT' || '|' || A.LOC_LV1_ID) IN ( 'VT|VT1', 'VT|VT2' )
	//                      |               |   ...
	//                      |---------------|--------------------------------------------------------------------------------------------------------/
	//                      | _TreeCond     |   AND ('VT' || '|' || A.FACTORY_CD) IN ( 'VT|VT1', 'VT|VT2' )
	//                      |       ByOriNm |   ...
	//                      |---------------|--------------------------------------------------------------------------------------------------------/
	//                      | _HrchyJoin    |   AND LH1.LOC_LV1_ID = A.LOC_LV1_ID
	//                      |               |   ...
	//                      |---------------|--------------------------------------------------------------------------------------------------------/
	//                      | _HrchyJoin    |   AND LH1.LOC_LV1 = A.FACTORY_CD
	//                      |       ByOriNm |   ...
	//----------------------|---------------|--------------------------------------------------------------------------------------------------------/
	// GroupBy  | Part #1   | _FromH_All    |   LH1.LOC_LV1_ID, LH1.LOC_LV1_NM, LH1.LOC_LV1_ORDB
	//          |           |               | , LH1.LOC_LV2_ID, LH1.LOC_LV2_NM, LH1.LOC_LV2_ORDB
	//          |           |               | , ...
	//          |           |               | , A.STYLE_NO_ID, A.STYLE_NO_NM, A.STYLE_NO_ORDB
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_All    |   A.LOC_LV1_ID, A.LOC_LV1_NM, A.LOC_LV1_ORDB
	//          |           |               | , A.LOC_LV2_ID, A.LOC_LV2_NM, A.LOC_LV2_ORDB
	//          |           |               | , ...
	//          |           |               | , A.STYLE_NO_ID, A.STYLE_NO_NM, A.STYLE_NO_ORDB
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_Col    |   A.LOC_LV1_ID
	//          |           |               | , A.LOC_LV2_ID
	//          |           |               | , ...
	//          |           |               | , A.STYLE_NO_ID, A.STYLE_NO_NM, A.STYLE_NO_ORDB
	//          |           |               | , ...
	//          |-----------|---------------|--------------------------------------------------------------------------------------------------------/
	//          | Part #2   | _FromA        | , A.MEASURE_ID, A.MEASURE_NM, A.MEASURE_SEQ
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromM        | , M.MEASURE_ID, M.MEASURE_NM, M.MEASURE_SEQ
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | (Empty)       |  (Dimension Column Only)
	//----------------------|---------------|--------------------------------------------------------------------------------------------------------/
	// GroupOnly| Part #1   | _FromH_All    |   Rollup 제외
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_All    |   Rollup 제외
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromS_All    |   Rollup 제외
	//          |-----------|---------------|--------------------------------------------------------------------------------------------------------/
	//          | Part #2   | _FromA        | , A.MEASURE_ID, A.MEASURE_NM, A.MEASURE_SEQ
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromM        | , M.MEASURE_ID, M.MEASURE_NM, M.MEASURE_SEQ
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | (Empty)       |  (Dimension Column Only)
	//----------|-----------|---------------|--------------------------------------------------------------------------------------------------------/
	// OrderBy  | Part #1   | _FromH_All    |   GROUPING(LH1.LOC_LV1_ID) DESC, LH1.LOC_LV1_ORDB, LH1.LOC_LV1_ID
	//          |           |               | , GROUPING(LH1.LOC_LV2_ID) DESC, LH1.LOC_LV2_ORDB, LH1.LOC_LV2_ID
	//          |           |               | , ...
	//          |           |               | , GROUPING(A.STYLE_NO_ID) DESC, A.STYLE_NO_ORDB, A.STYLE_NO_ID
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromA_All    |   GROUPING(A.LOC_LV1_ID) DESC, A.LOC_LV1_ORDB, A.LOC_LV1_ID
	//          |           |               | , GROUPING(A.LOC_LV2_ID) DESC, A.LOC_LV2_ORDB, A.LOC_LV2_ID
	//          |           |               | , ...
	//          |           |               | , GROUPING(A.STYLE_NO_ID) DESC, A.STYLE_NO_ORDB, A.STYLE_NO_ID
	//          |           |               | , ...
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromS_All    |   A.LOC_LV1_GRP DESC, A.LOC_LV1_ORDB, A.LOC_LV1_ID
	//          |           |               | , A.LOC_LV2_GRP DESC, A.LOC_LV2_ORDB, A.LOC_LV2_ID
	//          |           |               | , ...
	//          |           |               | , A.STYLE_NO_GRP DESC, A.STYLE_NO_ORDB, A.STYLE_NO_ID
	//          |           |               | , ...
	//          |-----------|---------------|--------------------------------------------------------------------------------------------------------/
	//          | Part #2   | _FromA        | , A.MEASURE_SEQ
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | _FromM        | , M.MEASURE_SEQ
	//          |           |---------------|--------------------------------------------------------------------------------------------------------/
	//          |           | (Empty)       |  (Dimension Column Only)
	//-----------------------------------------------------------------------------------------------------------------------------------------------/
	
	private String qgLangEx;
	private String qgUserRole;
	
	private String qgScrnType;
	
	private String qgMainTbAlias;
	private String qgMeasureTbAlias;
	
	private String qgComparePlanIdYn;
	private String qgComparePlanId1;
	private String qgComparePlanId2;
	
	private String qgCompareDmndIdYn;
	private String qgCompareDmndId1;
	private String qgCompareDmndId2;
	
	private String qgSelectDimension_FromH_All;
	private String qgSelectDimension_FromA_All;
	private String qgSelectDimension_FromA_ID;
	private String qgSelectDimension_FromA_Col;
	private String qgSelectDimension_FromS_All;
	private String qgSelectDimension_FromS_ID;
	private String qgSelectDimension_FromH_All_Nogrouping;
	private String qgSelectDimension_FromA_All_Nogrouping;
	private String qgSelectDimension_FromS_All_Nogrouping;
	private String qgSelectDimension_FromS_ID_Nogrouping;
	
	private String qgSelectMeasure_FromA;
	private String qgSelectMeasure_FromM;
	
	private String qgSelectBucket_FromA;
	private String qgSelectBucket_FromA_StockOnLastday;
	private String qgSelectSizeBucket_FromA_MdsVsPlanning;
	private String qgSelectSizeBucket_FromA_Rmain_DeliveryPlan;
	private String qgSelectBucket_FromA_MaterialPSI;
	private String qgSelectBucket_FromS;
	private String qgSelectSizeBucket_FromS_MdsVsPlanning;
	private String qgSelectBucket_FromA_Rmain;
	private String qgSelectBucket_FromA_Rmain_Zero;
	private String qgSelectBucket_FromA_Rsub;
	private String qgSelectBucket_FromA_ZeroDel;
	private String qgSelectSizeBucket_FromA_Rsub_DeliveryPlan;
	
	private String qgSelectBucket_FromA_CapaAnalysisTfp;
	
	private String qgFrom_HrchyTable;
	private String qgFrom_MeasureTable;
	
	private String qgWhere_TreeCond;
	private String qgWhere_TreeCondLine;
	private String qgWhere_TreeCondByOriNm;
	private String qgWhere_HrchyJoin;
	private String qgWhere_HrchyJoinByOriNm;
	
	private String qgGroupBy_FromH_All_FromA;
	private String qgGroupBy_FromH_All_FromM;
	private String qgGroupBy_FromH_All;
	private String qgGroupBy_FromA_All_FromA;
	private String qgGroupBy_FromA_All_FromM;
	private String qgGroupBy_FromA_All;
	private String qgGroupBy_FromA_ID_FromA;
	private String qgGroupBy_FromA_ID_FromM;
	private String qgGroupBy_FromA_ID;
	private String qgGroupBy_FromA_Col_FromA;
	private String qgGroupBy_FromA_Col_FromM;
	private String qgGroupBy_FromA_Col;
	private String qgGroupOnly_FromH_All_FromA;
	private String qgGroupOnly_FromH_All_FromM;
	private String qgGroupOnly_FromH_All;
	private String qgGroupOnly_FromA_All_FromA;
	private String qgGroupOnly_FromA_All_FromM;
	private String qgGroupOnly_FromA_All;
	private String qgGroupOnly_FromA_ID_FromA;
	private String qgGroupOnly_FromA_ID_FromM;
	private String qgGroupOnly_FromA_ID;
	private String qgGroupOnly_FromS_All;
	private String qgGroupOnly_FromS_ID;
	
	private String qgOrderBy_FromH_All_FromA;
	private String qgOrderBy_FromH_All_FromM;
	private String qgOrderBy_FromH_All_Desc_FromM;
	private String qgOrderBy_FromH_All;
	private String qgOrderBy_FromH_All_Desc;
	private String qgOrderBy_FromA_All_FromA;
	private String qgOrderBy_FromA_All_FromM;
	private String qgOrderBy_FromA_All;
	private String qgOrderBy_FromS_All_FromA;
	private String qgOrderBy_FromS_All_FromM;
	private String qgOrderBy_FromS_All;
	private String qgOrderBy_FromS_ID_PlanResult_Tfp;
	private String qgOrderBy_FromS_ID_FromA;
	private String qgOrderBy_FromS_ID_FromM;
	private String qgOrderBy_FromS_ID;
	
	private String qgPattern_A_Top;
	private String qgPattern_A_Bottom;
	private String qgPattern_B_Top;
	private String qgPattern_B_Bottom;
	private String qgPattern_C_Top;
	private String qgPattern_C_Bottom;
	private String qgPattern_D_Top;
	private String qgPattern_D_Bottom;
	
	private String qgHolidayStr = "";
	private List<Map<String, Object>> optFrozenPeriod = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> optEnhancePeriod = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> optEnhancePreBuild = new ArrayList<Map<String, Object>>();
	
	private List<Map<String, Object>> qgCalcField = new ArrayList<Map<String, Object>>();
	
	/**
	 * Query Generate
	 */
	public Map<String, Object> getQgParamMap(CommonInVo inVo) {
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> dsDimension    = inVo.getInList("dsDimension");
		List<Map<String, Object>> dsMeasure      = inVo.getInList("dsMeasure");
		List<Map<String, Object>> dsBucket       = inVo.getInList("dsBucket");
		List<Map<String, Object>> dsSizeBucket   = inVo.getInList("dsSizeBucket");
		List<Map<String, Object>> dsTree         = inVo.getInList("dsTree");
		
		// Language, User Role, Table Alias
		setLangEx();
		setUserRole();
		setTbAlias(inVo);
		setScrnType(inVo);
		setComparePlanId(inVo);
		setCompareDmndId(inVo);
		setQgCalcField(inVo, dsMeasure);
		setHolidayStr(dsBucket);
		
		// Get All Dimension List
		inVo.putInParam("langEx", getLangEx());
		
		String queryId = "com.tkg.gscm.sample.dao.CommonDao.com-cello-tk-dao-commonDao-selectAllDimensionList";
		List<Map<String, Object>> dsAllDimension = dao.selectList(queryId, inVo.getInParamMap());
		
		// SELECT
		setSelectDimension_FromH_All(dsDimension);
		setSelectDimension_FromA_All(dsDimension);
		setSelectDimension_FromA_ID(dsDimension);
		setSelectDimension_FromA_Col(dsAllDimension, dsDimension);
		setSelectDimension_FromS_All(dsDimension);
		setSelectDimension_FromS_ID(dsDimension);
		setSelectDimension_FromH_All_Nogrouping(dsDimension);
		setSelectDimension_FromA_All_Nogrouping(dsDimension);
		setSelectDimension_FromS_All_Nogrouping(dsDimension);
		setSelectDimension_FromS_ID_Nogrouping(dsDimension);
		setSelectMeasure_FromA();
		setSelectMeasure_FromM();
		setSelectBucket_FromA(dsBucket, dsMeasure);
		setSelectBucket_FromA_StockOnLastday(dsBucket, dsMeasure);
		setSelectSizeBucket_FromA_MdsVsPlanning(dsSizeBucket, dsMeasure);
		setSelectSizeBucket_FromA_Rmain_DeliveryPlan(dsSizeBucket, dsMeasure);
		setSelectBucket_FromA_MaterialPSI(dsBucket, dsMeasure);
		setSelectBucket_FromS(dsBucket, dsMeasure);
		setSelectSizeBucket_FromS_MdsVsPlanning(dsSizeBucket, dsMeasure);
		setSelectBucket_FromA_Rmain(dsBucket, dsMeasure);
		setSelectBucket_FromA_Rmain_Zero(dsBucket, dsMeasure);
		setSelectBucket_FromA_Rsub(dsBucket, dsMeasure);
		setSelectBucket_FromA_ZeroDel(dsBucket, dsMeasure);
		setSelectSizeBucket_FromA_Rsub_DeliveryPlan(dsSizeBucket, dsMeasure);
		
		setSelectBucket_FromA_CapaAnalysisTfp(dsBucket, dsMeasure);
		
		// FROM
		setFrom_HrchyTable(dsDimension);
		setFrom_MeasureTable(dsMeasure);
		
		// WHERE
		setWhere_TreeCond(dsTree);
		setWhere_TreeCondLine(dsTree);
		setWhere_TreeCondByOriNm(dsTree);
		setWhere_HrchyJoin(dsAllDimension, dsDimension);
		setWhere_HrchyJoinByOriNm(dsAllDimension, dsDimension);
		
		// GROUP BY
		setGroupBy_FromH_All_FromA(dsDimension);
		setGroupBy_FromH_All_FromM(dsDimension);
		setGroupBy_FromH_All(dsDimension);
		setGroupBy_FromA_All_FromA(dsDimension);
		setGroupBy_FromA_All_FromM(dsDimension);
		setGroupBy_FromA_All(dsDimension);
		setGroupBy_FromA_ID_FromA(dsDimension);
		setGroupBy_FromA_ID_FromM(dsDimension);
		setGroupBy_FromA_ID(dsDimension);
		setGroupBy_FromA_Col_FromA(dsAllDimension, dsDimension);
		setGroupBy_FromA_Col_FromM(dsAllDimension, dsDimension);
		setGroupBy_FromA_Col(dsAllDimension, dsDimension);
		setGroupOnly_FromH_All_FromA(dsDimension);
		setGroupOnly_FromH_All_FromM(dsDimension);
		setGroupOnly_FromH_All(dsDimension);
		setGroupOnly_FromA_All_FromA(dsDimension);
		setGroupOnly_FromA_All_FromM(dsDimension);
		setGroupOnly_FromA_All(dsDimension);
		setGroupOnly_FromA_ID_FromA(dsDimension);
		setGroupOnly_FromA_ID_FromM(dsDimension);
		setGroupOnly_FromA_ID(dsDimension);
		setGroupOnly_FromS_All(dsDimension);
		setGroupOnly_FromS_ID(dsDimension);
		
		// ORDER BY
		setOrderBy_FromH_All_FromA(dsDimension);
		setOrderBy_FromH_All_FromM(dsDimension);
		setOrderBy_FromH_All_Desc_FromM(dsDimension);
		setOrderBy_FromH_All(dsDimension);
		setOrderBy_FromH_All_Desc(dsDimension);
		setOrderBy_FromA_All_FromA(dsDimension);
		setOrderBy_FromA_All_FromM(dsDimension);
		setOrderBy_FromA_All(dsDimension);
		setOrderBy_FromS_All_FromA(dsDimension);
		setOrderBy_FromS_All_FromM(dsDimension);
		setOrderBy_FromS_All(dsDimension);
		setOrderBy_FromS_ID_PlanResult_Tfp(dsDimension);
		setOrderBy_FromS_ID_FromA(dsDimension);
		setOrderBy_FromS_ID_FromM(dsDimension);
		setOrderBy_FromS_ID(dsDimension);
		
		// TOTAL
		setPattern_A_Top();
		setPattern_A_Bottom();
		setPattern_B_Top();
		setPattern_B_Bottom();
		setPattern_C_Top();
		setPattern_C_Bottom();
		setPattern_D_Top();
		setPattern_D_Bottom();
		
		
		// paramMap Setting
		/*
		paramMap.putAll(inVo.getInParamMap());
		
		Iterator<String> iterator = inVo.getInListIterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			
			paramMap.put(key, inVo.getInList(key));
		}
		*/
		 
		paramMap = commonService.setCommonCondition(inVo, paramMap);
		
		paramMap.put("qglangEx",                    getLangEx());
		paramMap.put("qgUserRole",                  getUserRole());
		paramMap.put("qgMainTbAlias",               getMainTbAlias());
		paramMap.put("qgMeasureTbAlias",            getMeasureTbAlias());
		paramMap.put("qgComparePlanIdYn",           getComparePlanIdYn());
		paramMap.put("qgComparePlanId1",            getComparePlanId1());
		paramMap.put("qgComparePlanId2",            getComparePlanId2());
		paramMap.put("qgCompareDmndIdYn",           getCompareDmndIdYn());
		paramMap.put("qgCompareDmndId1",            getCompareDmndId1());
		paramMap.put("qgCompareDmndId2",            getCompareDmndId2());
		paramMap.put("qgCalcField",                 getCalcField());
		paramMap.put("qgHolidayStr",                getHolidayStr());
		paramMap.put("optFrozenPeriod",                optFrozenPeriod);
		paramMap.put("optEnhancePeriod",                optEnhancePeriod);
		paramMap.put("optEnhancePreBuild",                optEnhancePreBuild);
		
		paramMap.put("qgSelectDimension_FromH_All", getSelectDimension_FromH_All());
		paramMap.put("qgSelectDimension_FromA_All", getSelectDimension_FromA_All());
		paramMap.put("qgSelectDimension_FromA_ID",  getSelectDimension_FromA_ID());
		paramMap.put("qgSelectDimension_FromA_Col", getSelectDimension_FromA_Col());
		paramMap.put("qgSelectDimension_FromS_All", getSelectDimension_FromS_All());
		paramMap.put("qgSelectDimension_FromS_ID",  getSelectDimension_FromS_ID());
		paramMap.put("qgSelectDimension_FromH_All_Nogrouping", getSelectDimension_FromH_All_Nogrouping());
		paramMap.put("qgSelectDimension_FromA_All_Nogrouping", getSelectDimension_FromA_All_Nogrouping());
		paramMap.put("qgSelectDimension_FromS_All_Nogrouping", getSelectDimension_FromS_All_Nogrouping());
		paramMap.put("qgSelectDimension_FromS_ID_Nogrouping",  getSelectDimension_FromS_ID_Nogrouping());
		paramMap.put("qgSelectMeasure_FromA",       getSelectMeasure_FromA());
		paramMap.put("qgSelectMeasure_FromM",       getSelectMeasure_FromM());
		paramMap.put("qgSelectBucket_FromA",        getSelectBucket_FromA());
		paramMap.put("qgSelectBucket_FromA_StockOnLastday",        getSelectBucket_FromA_StockOnLastday());
		paramMap.put("qgSelectSizeBucket_FromA_MdsVsPlanning",        getSelectSizeBucket_FromA_MdsVsPlanning());
		paramMap.put("qgSelectSizeBucket_FromA_Rmain_DeliveryPlan",        getSelectSizeBucket_FromA_Rmain_DeliveryPlan());
		paramMap.put("qgSelectBucket_FromA_MaterialPSI",        getSelectBucket_FromA_MaterialPSI());
		paramMap.put("qgSelectBucket_FromS",        getSelectBucket_FromS());
		paramMap.put("qgSelectSizeBucket_FromS_MdsVsPlanning",        getSelectSizeBucket_FromS_MdsVsPlanning());
		paramMap.put("qgSelectBucket_FromA_Rmain",  getSelectBucket_FromA_Rmain());
		paramMap.put("qgSelectBucket_FromA_Rmain_Zero",  getSelectBucket_FromA_Rmain_Zero());
		paramMap.put("qgSelectBucket_FromA_Rsub",   getSelectBucket_FromA_Rsub());
		paramMap.put("qgSelectBucket_FromA_ZeroDel",   getSelectBucket_FromA_ZeroDel());
		paramMap.put("qgSelectSizeBucket_FromA_Rsub_DeliveryPlan",   getSelectSizeBucket_FromA_Rsub_DeliveryPlan());
		paramMap.put("qgSelectBucket_FromA_CapaAnalysisTfp",        getSelectBucket_FromA_CapaAnalysisTfp());
		paramMap.put("qgFrom_HrchyTable",           getFrom_HrchyTable());
		paramMap.put("qgFrom_MeasureTable",         getFrom_MeasureTable());
		paramMap.put("qgWhere_TreeCond",            getWhere_TreeCond());
		paramMap.put("qgWhere_TreeCondLine",        getWhere_TreeCondLine());
		paramMap.put("qgWhere_TreeCondByOriNm",     getWhere_TreeCondByOriNm());
		paramMap.put("qgWhere_HrchyJoin",           getWhere_HrchyJoin());
		paramMap.put("qgWhere_HrchyJoinByOriNm",    getWhere_HrchyJoinByOriNm());
		paramMap.put("qgGroupBy_FromH_All_FromA",   getGroupBy_FromH_All_FromA());
		paramMap.put("qgGroupBy_FromH_All_FromM",   getGroupBy_FromH_All_FromM());
		paramMap.put("qgGroupBy_FromH_All",         getGroupBy_FromH_All());
		paramMap.put("qgGroupBy_FromA_All_FromA",   getGroupBy_FromA_All_FromA());
		paramMap.put("qgGroupBy_FromA_All_FromM",   getGroupBy_FromA_All_FromM());
		paramMap.put("qgGroupBy_FromA_All",         getGroupBy_FromA_All());
		paramMap.put("qgGroupBy_FromA_ID_FromA",    getGroupBy_FromA_ID_FromA());
		paramMap.put("qgGroupBy_FromA_ID_FromM",    getGroupBy_FromA_ID_FromM());
		paramMap.put("qgGroupBy_FromA_ID",          getGroupBy_FromA_ID());
		paramMap.put("qgGroupBy_FromA_Col_FromA",   getGroupBy_FromA_Col_FromA());
		paramMap.put("qgGroupBy_FromA_Col_FromM",   getGroupBy_FromA_Col_FromM());
		paramMap.put("qgGroupBy_FromA_Col",         getGroupBy_FromA_Col());
		paramMap.put("qgGroupOnly_FromH_All_FromA", getGroupOnly_FromH_All_FromA());
		paramMap.put("qgGroupOnly_FromH_All_FromM", getGroupOnly_FromH_All_FromM());
		paramMap.put("qgGroupOnly_FromH_All",       getGroupOnly_FromH_All());
		paramMap.put("qgGroupOnly_FromA_All_FromA", getGroupOnly_FromA_All_FromA());
		paramMap.put("qgGroupOnly_FromA_All_FromM", getGroupOnly_FromA_All_FromM());
		paramMap.put("qgGroupOnly_FromA_All",       getGroupOnly_FromA_All());
		paramMap.put("qgGroupOnly_FromA_ID_FromA",  getGroupOnly_FromA_ID_FromA());
		paramMap.put("qgGroupOnly_FromA_ID_FromM",  getGroupOnly_FromA_ID_FromM());
		paramMap.put("qgGroupOnly_FromA_ID",        getGroupOnly_FromA_ID());
		paramMap.put("qgGroupOnly_FromS_All",       getGroupOnly_FromS_All());
		paramMap.put("qgGroupOnly_FromS_ID",        getGroupOnly_FromS_ID());
		paramMap.put("qgOrderBy_FromH_All_FromA",   getOrderBy_FromH_All_FromA());
		paramMap.put("qgOrderBy_FromH_All_FromM",   getOrderBy_FromH_All_FromM());
		paramMap.put("qgOrderBy_FromH_All_Desc_FromM",   getOrderBy_FromH_All_Desc_FromM());
		paramMap.put("qgOrderBy_FromH_All",         getOrderBy_FromH_All());
		paramMap.put("qgOrderBy_FromH_All_Desc",         getOrderBy_FromH_All_Desc());
		paramMap.put("qgOrderBy_FromA_All_FromA",   getOrderBy_FromA_All_FromA());
		paramMap.put("qgOrderBy_FromA_All_FromM",   getOrderBy_FromA_All_FromM());
		paramMap.put("qgOrderBy_FromA_All",         getOrderBy_FromA_All());
		paramMap.put("qgOrderBy_FromS_All_FromA",   getOrderBy_FromS_All_FromA());
		paramMap.put("qgOrderBy_FromS_All_FromM",   getOrderBy_FromS_All_FromM());
		paramMap.put("qgOrderBy_FromS_All",         getOrderBy_FromS_All());
		paramMap.put("qgOrderBy_FromS_ID_PlanResult_Tfp",getOrderBy_FromS_ID_PlanResult_Tfp());
		paramMap.put("qgOrderBy_FromS_ID_FromA",    getOrderBy_FromS_ID_FromA());
		paramMap.put("qgOrderBy_FromS_ID_FromM",    getOrderBy_FromS_ID_FromM());
		paramMap.put("qgOrderBy_FromS_ID",          getOrderBy_FromS_ID());
		
		paramMap.put("qgPattern_A_Top",        getPattern_A_Top());
		paramMap.put("qgPattern_A_Bottom",     getPattern_A_Bottom());
		paramMap.put("qgPattern_B_Top",        getPattern_B_Top());
		paramMap.put("qgPattern_B_Bottom",     getPattern_B_Bottom());
		paramMap.put("qgPattern_C_Top",        getPattern_C_Top());
		paramMap.put("qgPattern_C_Bottom",     getPattern_C_Bottom());
		paramMap.put("qgPattern_D_Top",        getPattern_D_Top());
		paramMap.put("qgPattern_D_Bottom",     getPattern_D_Bottom());
		
		return paramMap;
	}
	
	/**
	 * Language Setting - qgLangEx
	 */
	private void setLangEx() {
		Map<String, String> languageMap = this.aspectBean.languageMap;
		String          defaultLanguage = this.aspectBean.defaultLanguage;
		
		String langCd = "";
		
		try {
			langCd = SessionUtil.getLangCd();
			
		} catch (Exception e) {
			langCd = defaultLanguage;
		}
		
		if (!languageMap.containsKey(langCd)) {
			langCd = defaultLanguage;
		}
		
		this.qgLangEx = (String) languageMap.get(langCd);
		
	}
	
	/**
	 * User Role Setting - qgUserRole
	 */
	private void setUserRole() {
		this.qgUserRole = codeService.getRoleString();
	}
	
	/**
	 * Table Alias Setting - qgMainTbAlias
	 *                       qgMeasureTbAlias
	 */
	public void setTbAlias(CommonInVo inVo) {
		String    mainTbAlias = inVo.getInParam(   "mainTbAlias");
		String measureTbAlias = inVo.getInParam("measureTbAlias");
		
		if (StringUtils.isBlank(   mainTbAlias))    mainTbAlias = "A";
		if (StringUtils.isBlank(measureTbAlias)) measureTbAlias = "M";
		
		this.qgMainTbAlias    = mainTbAlias;
		this.qgMeasureTbAlias = measureTbAlias;
	}

	/**
	 * Compare Plan ID Setting - qgComparePlanIdYn
	 *                           qgComparePlanId1
	 *                           qgComparePlanId2
	 */
	private void setScrnType(CommonInVo inVo) {
		String scrnType = inVo.getInParam("scrnType");
		
		if (StringUtils.isBlank(scrnType)) scrnType = "";
		
		this.qgScrnType = scrnType;
	}
	
	/**
	 * Compare Plan ID Setting - qgComparePlanIdYn
	 *                           qgComparePlanId1
	 *                           qgComparePlanId2
	 */
	private void setComparePlanId(CommonInVo inVo) {
		String comparePlanIdYn = inVo.getInParam("comparePlanIdYn");
		String comparePlanId1  = inVo.getInParam("comparePlanId1");
		String comparePlanId2  = inVo.getInParam("comparePlanId2");
		
		if (StringUtils.isBlank(comparePlanIdYn)) comparePlanIdYn = "N";
		
		if (StringUtils.isBlank(comparePlanId1) || StringUtils.isBlank(comparePlanId2)) comparePlanIdYn = "N";
		
		this.qgComparePlanIdYn = comparePlanIdYn;
		this.qgComparePlanId1  = comparePlanId1;
		this.qgComparePlanId2  = comparePlanId2;
	}
	
	/**
	 * Compare Dmnd ID Setting - qgCompareDmndIdYn
	 *                           qgCompareDmndId1
	 *                           qgCompareDmndId2
	 */
	private void setCompareDmndId(CommonInVo inVo) {
		String compareDmndIdYn = inVo.getInParam("compareDmndIdYn");
		String compareDmndId1  = inVo.getInParam("compareDmndId1");
		String compareDmndId2  = inVo.getInParam("compareDmndId2");
		
		if (StringUtils.isBlank(compareDmndIdYn)) compareDmndIdYn = "N";
		
		if (StringUtils.isBlank(compareDmndId1) || StringUtils.isBlank(compareDmndId2)) compareDmndIdYn = "N";
		
		this.qgCompareDmndIdYn = compareDmndIdYn;
		this.qgCompareDmndId1  = compareDmndId1;
		this.qgCompareDmndId2  = compareDmndId2;
	}
	
	/**
	 * Calcurate Fields Setting - qgCalcField
	 */
	private void setQgCalcField(CommonInVo inVo, List<Map<String, Object>> dsMeasure) {
		List<Map<String, Object>> dsCalc = inVo.getInList("dsCalc");
		
		this.qgCalcField.clear();
		
		if (dsCalc != null) {
			for (int i = 0; i < dsCalc.size(); i++) {
				Map<String, Object> map = dsCalc.get(i);
				
				String target  = map.get("target")    + "";
				String source1 = map.get("source1")   + "";
				String source2 = map.get("source2")   + "";
				
				if (isInMeasure(target, dsMeasure) && isInMeasure(source1, dsMeasure) && isInMeasure(source2, dsMeasure)) {
					this.qgCalcField.add(map);
				}
			}
		}
	}
	
	/**
	 * Holiday Setting - qgHolidayStr
	 */
	private void setHolidayStr(List<Map<String, Object>> dsBucket) {
		String qgHolidayStr = "";
		if (dsBucket != null) {
			for (int i = 0; i < dsBucket.size(); i++) {
				Map<String, Object> map = dsBucket.get(i);
				
				if (map.containsKey("BUKVALUE") && map.containsKey("HOLIDAY_TYPE")) {
					String holidayType = String.valueOf(map.get("HOLIDAY_TYPE"));
					
					if ("H".equals(holidayType)) {
						String ymd = map.get("BUKVALUE").toString();
						
						if (qgHolidayStr.length() > 0) {
							qgHolidayStr += ",";
						}
						
						qgHolidayStr += ymd;
					}
				}
			}
		}
		
		this.qgHolidayStr = qgHolidayStr;
		
		log.debug("qgHolidayStr=" + getHolidayStr());
	}
	
	private boolean isInMeasure(String str, List<Map<String, Object>> dsMeasure) {
		boolean isIn = false;
		
		for (int i = 0; i < dsMeasure.size(); i++) {
			Map<String, Object> map = dsMeasure.get(i);
			
			String strMeasureNm = map.get("MEASURE_NM") + "";
			
			if (str.equals(strMeasureNm)) {
				isIn = true;
				
				break;
			}
		}
		
		return isIn;
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromH_All
	 */
	private void setSelectDimension_FromH_All(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		String  upperGroupCol = "";
		
		strQuery.append("--qgSelectDimension_FromH_All Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attribute)
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR(Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";
			
			String idCol = "";
			String nmCol = "";
			String obCol = "";
			
			String gpAs  = strDimColId + "_GRP";
			String nmAs  = strDimColId + "_NM";
			
			if ("Y".equals(getCompareDmndIdYn()) && (strDimColId.equals("TGAC") || strDimColId.equals("HOLD_YN") || strDimColId.equals("CLOSE_YN"))) {			
			}else if("N".equals(strGrpByYn)){
				if (i > 0) {
					strQuery.append("\t\t       , ");
				} else {
					strQuery.append("\t\t         ");
				}

				idCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ID";
				nmCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_NM";
				obCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ORDB";
				
				strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
			}else {
					if (i > 0) {
						strQuery.append("\t\t       , ");
					} else {
						strQuery.append("\t\t         ");
					}
					
					if ("ATTR".equals(strDmsnTcd)) {
						idCol = getMainTbAlias() + "." + strDimColId + "_ID";
						nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
						obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
					} else {
						idCol = strDmsnHrchyTcd + "." + strDimColId + "_ID";
						nmCol = strDmsnHrchyTcd + "." + strDimColId + "_NM" + getLangEx();
						obCol = strDmsnHrchyTcd + "." + strDimColId + "_ORDB";
					}
					
					strQuery.append(idCol + ", GROUPING(" + idCol + ") AS " + gpAs + ", ");
					
					if (StringUtils.isEmpty(upperGroupCol)) {
						strQuery.append("CASE WHEN GROUPING(" + idCol + ") = 1 THEN 'Total' ELSE " + nmCol + " END AS " + nmAs + ", ");
					} else {
						strQuery.append("CASE WHEN GROUPING(" + idCol + ") = 1 AND GROUPING(" + upperGroupCol + ") = 0 THEN 'Total' ELSE " + nmCol + " END AS " + nmAs + ", ");
					}
					upperGroupCol = idCol;
					
					strQuery.append(obCol + "\n");
			}
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromH_All End\n");
		
		this.qgSelectDimension_FromH_All = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromH_All_Nogrouping
	 */
	private void setSelectDimension_FromH_All_Nogrouping(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectDimension_FromH_All_Nogrouping Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attribute)
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR(Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";
			
			String idCol = "";
			String nmCol = "";
			String obCol = "";
			
			if (i > 0) {
				strQuery.append("\t\t       , ");
			} else {
				strQuery.append("\t\t         ");
			}
			
			if("N".equals(strGrpByYn)){
				idCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_ID";
				nmCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_NM";
				obCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_ORDB";
			} else {
				if ("ATTR".equals(strDmsnTcd)) {
					idCol = getMainTbAlias() + "." + strDimColId + "_ID";
					nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
					obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
				} else {
					idCol = strDmsnHrchyTcd + "." + strDimColId + "_ID";
					nmCol = strDmsnHrchyTcd + "." + strDimColId + "_NM" + getLangEx();
					obCol = strDmsnHrchyTcd + "." + strDimColId + "_ORDB";
				}
			} 
			
			strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromH_All_Nogrouping End\n");
		
		this.qgSelectDimension_FromH_All_Nogrouping = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromA_All
	 */
	private void setSelectDimension_FromA_All(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		String  upperGroupCol = "";
		
		strQuery.append("--qgSelectDimension_FromA_All Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";
			
			String idCol = "";
			String nmCol = "";
			String obCol = "";

			String gpAs  = strDimColId + "_GRP";
			String nmAs  = strDimColId + "_NM";

			if (i > 0) {
				strQuery.append("\t\t       , ");
			} else {
				strQuery.append("\t\t         ");
			}

			if("N".equals(strGrpByYn)){
				idCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ID";
				nmCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_NM";
				obCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ORDB";
				
				strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
			} else {
				idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
				obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";

				strQuery.append(idCol + ", GROUPING(" + idCol + ") AS " + gpAs + ", ");
				
				if (StringUtils.isEmpty(upperGroupCol)) {
					strQuery.append("CASE WHEN GROUPING(" + idCol + ") = 1 THEN 'Total' ELSE " + nmCol + " END AS " + nmAs + ", ");
				} else {
					strQuery.append("CASE WHEN GROUPING(" + idCol + ") = 1 AND GROUPING(" + upperGroupCol + ") = 0 THEN 'Total' ELSE " + nmCol + " END AS " + nmAs + ", ");
				}
				upperGroupCol = idCol;
				
				strQuery.append(obCol + "\n");
			}
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromA_All End\n");
		
		this.qgSelectDimension_FromA_All = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromA_ID
	 */
	private void setSelectDimension_FromA_ID(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		String  upperGroupCol = "";
		
		strQuery.append("--qgSelectDimension_FromA_ID Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";
			
			String idCol = "";
			String nmCol = "";
			
			String gpAs  = strDimColId + "_GRP";
			String nmAs  = strDimColId + "_NM";

			if (i > 0) {
				strQuery.append("\t\t       , ");
			} else {
				strQuery.append("\t\t         ");
			}

			if("N".equals(strGrpByYn)){
				idCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ID";
				nmCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_NM";
				
				strQuery.append(idCol + ", " + nmCol + ", " + "\n");
			} else {
				idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				nmCol = getMainTbAlias() + "." + strDimColId + "_ID";
				//String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
				
				if ("MODEL_ID".equals(strDimColId)) {
					nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
				}
				
				strQuery.append(idCol + ", GROUPING(" + idCol + ") AS " + gpAs + ", ");
				
				if (StringUtils.isEmpty(upperGroupCol)) {
					strQuery.append("CASE WHEN GROUPING(" + idCol + ") = 1 THEN 'Total' ELSE " + nmCol + " END AS " + nmAs + "\n");
				} else {
					strQuery.append("CASE WHEN GROUPING(" + idCol + ") = 1 AND GROUPING(" + upperGroupCol + ") = 0 THEN 'Total' ELSE " + nmCol + " END AS " + nmAs + "\n");
				}
				upperGroupCol = idCol;
			}
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromA_ID End\n");
		
		this.qgSelectDimension_FromA_ID = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromS_All
	 */
	private void setSelectDimension_FromS_All(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectDimension_FromS_All Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";

			String idCol = "";
			String gpCol = "";
			String nmCol = "";
			String obCol = "";

			if (i > 0) {
				strQuery.append("\t\t       , ");
			} else {
				strQuery.append("\t\t         ");
			}

			if("N".equals(strGrpByYn)){
				idCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ID";
				gpCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_GRP";
				nmCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_NM";
				obCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ORDB";
			} else {
				idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				gpCol = getMainTbAlias() + "." + strDimColId + "_GRP";
				nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
				obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			}
			strQuery.append(idCol + ", " + gpCol + ", " + nmCol + ", " + obCol + "\n");
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromS_All End\n");
		
		this.qgSelectDimension_FromS_All = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromS_ID
	 */
	private void setSelectDimension_FromS_ID(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectDimension_FromS_ID Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";

			String idCol = "";
			String gpCol = "";
			String nmCol = "";

			if (i > 0) {
				strQuery.append("\t\t       , ");
			} else {
				strQuery.append("\t\t         ");
			}

			if("N".equals(strGrpByYn)){
				idCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ID";
				gpCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_GRP";
				nmCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_NM";
			} else {
				idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				gpCol = getMainTbAlias() + "." + strDimColId + "_GRP";
				nmCol = "MAX(" + getMainTbAlias() + "." + strDimColId + "_NM" + ") AS " + strDimColId + "_NM";
				//String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			}
			
			strQuery.append(idCol + ", " + gpCol + ", " + nmCol + "\n");
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromS_ID End\n");
		
		this.qgSelectDimension_FromS_ID = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromA_All_Nogrouping
	 */
	private void setSelectDimension_FromA_All_Nogrouping(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectDimension_FromA_All_Nogrouping Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";
			
			String idCol = "";
			String nmCol = "";
			String obCol = "";
			
			String gpAs  = strDimColId + "_GRP";
			String nmAs  = strDimColId + "_NM";

			if (i > 0) {
				strQuery.append("\t\t       , ");
			} else {
				strQuery.append("\t\t         ");
			}

			if("N".equals(strGrpByYn)){
				idCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_ID";
				nmCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_NM";
				obCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_ORDB";
			} else {
				idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
				obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			}
			
			strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromA_All_Nogrouping End\n");
		
		this.qgSelectDimension_FromA_All_Nogrouping = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromS_All_Nogrouping
	 */
	private void setSelectDimension_FromS_All_Nogrouping(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectDimension_FromS_All_Nogrouping Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";

			String idCol = "";
			String nmCol = "";
			String obCol = "";
			
			if (i > 0) {
				strQuery.append("\t\t       , ");
			} else {
				strQuery.append("\t\t         ");
			}

			if("N".equals(strGrpByYn)){
				idCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_ID";
				nmCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_NM";
				obCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_ORDB";
			} else {
				idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
				obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			}
			
			strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromS_All_Nogrouping End\n");
		
		this.qgSelectDimension_FromS_All_Nogrouping = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromS_ID_Nogrouping
	 */
	private void setSelectDimension_FromS_ID_Nogrouping(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectDimension_FromS_ID_Nogrouping Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";

			String idCol = "";
			
			if (i > 0) {
				strQuery.append("\t\t       , ");
			} else {
				strQuery.append("\t\t         ");
			}
			
			if("N".equals(strGrpByYn)){
				idCol = getMainTbAlias() + "." + strDimColId + " AS " + strDimColId + "_ID";
			} else {
				idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				
				if (("MODEL_ID").equals(strDimColId)) {
					idCol += ", " + getMainTbAlias() + "." + strDimColId + "_NM";
				}
			}
			
			strQuery.append(idCol + "\n");
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromS_ID_Nogrouping End\n");
		
		this.qgSelectDimension_FromS_ID_Nogrouping = strQuery.toString();
	}
	
	/**
	 * [SELECT] Dimension List Setting - qgSelectDimension_FromA_Col
	 */
	private void setSelectDimension_FromA_Col(List<Map<String, Object>> dsAllDimension, List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectDimension_FromA_Col Start\n");
		
		boolean bFirst = true;
		
		// Except Attribute
		for (int i = 0; i < dsAllDimension.size(); i++) {
			Map<String, Object> map = dsAllDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR(Attribute)
			
			if (!"ATTR".equals(strDmsnTcd)) {
				String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				
				if (bFirst) {
					strQuery.append("\t\t         ");
					
					bFirst = false;
				} else {
					strQuery.append("\t\t       , ");
				}
				
				strQuery.append(idCol + "\n");
			}
		}
		
		// Attribute
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR(Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			String strGrpType      = map.get("GROUP_TYPE")     + "";
			
			String idCol = "";
			String nmCol = "";
			String obCol = "";
			
			if (bFirst) {
				strQuery.append("\t\t         ");
				bFirst = false;
			} else {
				strQuery.append("\t\t       , ");
			}
			
			if("N".equals(strGrpByYn)){
				idCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ID";
				nmCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_NM";
				obCol = strGrpType + "(" + getMainTbAlias() + "." + strDimColId + ") AS " + strDimColId + "_ORDB";
				
				strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
			} else {
				if ("ATTR".equals(strDmsnTcd)) {
					idCol = getMainTbAlias() + "." + strDimColId + "_ID";
					nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
					obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
					
					strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
				}
			}
		}
		
		strQuery.append("\t\t       --qgSelectDimension_FromA_Col End\n");
		
		this.qgSelectDimension_FromA_Col = strQuery.toString();
	}
	
	/**
	 * [SELECT] Measure List Setting - qgSelectMeasure_FromA
	 */
	private void setSelectMeasure_FromA() {
		this.qgSelectMeasure_FromA = getSelectMeasure("FromA");
	}
	
	/**
	 * [SELECT] Measure List Setting - qgSelectMeasure_FromM
	 */
	private void setSelectMeasure_FromM() {
		this.qgSelectMeasure_FromM = getSelectMeasure("FromM");
	}
	
	private String getSelectMeasure(String from) {
		StringBuffer strQuery = new StringBuffer();
		
		String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
		
		strQuery.append( "--qgSelectMeasure_FromA Start\n");
		
		if ("Y".equals(getComparePlanIdYn())) {
			strQuery.append("\t\t       , " + alias + ".COMP_PLAN_ID\n");
			strQuery.append("\t\t       , " + alias + ".COMP_PLAN_SEQ\n");
		}
		
		strQuery.append( ""
			+ "\t\t       , " + alias + ".MEASURE_ID\n"
			+ "\t\t       , " + alias + ".MEASURE_NM\n"
			+ "\t\t       , " + alias + ".MEASURE_SEQ\n"
			+ "\t\t       --qgSelectMeasure_FromA End\n"
		);
		
		return strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectBucket_FromA
	 */
	private void setSelectBucket_FromA(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			String strBuktype = mapBucket.get("BUKTYPE") + "";		// W, D, ...
			String strBukCol  = "";
			
			if ("D".equals(strBuktype)) {
				strBukCol = "DAY";
			} else if ("W".equals(strBuktype)) {
				strBukCol = "WEEK";
			} else if ("PW".equals(strBuktype)) {
				strBukCol = "PARTIAL_WEEK";
			} else if ("M".equals(strBuktype)) {
				strBukCol = "MONTH";
			} else if ("Y".equals(strBuktype)) {
				strBukCol = "YR";
			}
			
			strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t                              ");
				}
				
				if ("Y".equals(getComparePlanIdYn())) {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " DECODE(" + getMeasureTbAlias() + ".COMP_PLAN_SEQ, '3', SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", '" + getComparePlanId1() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) - SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", '" + getComparePlanId2() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)), SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", " + getMeasureTbAlias() + ".COMP_PLAN_ID || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0))),\n");
				} else {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)),\n");
				}
			}
			
			strQuery.append("\t\t         NULL) AS " + strTimebuk + "\n");
		}
		
		strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (j > 0) {
				strQuery.append("\t\t                              ");
			}
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " DECODE(" + getMeasureTbAlias() + ".COMP_PLAN_SEQ, '3', SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, '" + getComparePlanId1() + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) - SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, '" + getComparePlanId2() + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)), SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, " + getMeasureTbAlias() + ".COMP_PLAN_ID, " + getMainTbAlias() + "." + strMeasureNm + ", 0))),\n");
			} else {
				strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " SUM(" + getMainTbAlias() + "." + strMeasureNm + "),\n");
			}
		}
		
		strQuery.append("\t\t         NULL) AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --qgSelectBucket_FromA End\n");
		
		this.qgSelectBucket_FromA = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectBucket_FromA_StockOnLastday
	 */
	private void setSelectBucket_FromA_StockOnLastday(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA_StockOnLastday Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			String strBuktype = mapBucket.get("BUKTYPE") + "";		// W, D, ...
			String strBukCol  = "";
			String strLastdayBukCol = "";
			
			if ("D".equals(strBuktype)) {
				strBukCol = "DAY";
				strLastdayBukCol = "DAY";
			} else if ("W".equals(strBuktype)) {
				strBukCol = "WEEK";
				strLastdayBukCol = "WEEK_ON_LASTDAY";
			} else if ("PW".equals(strBuktype)) {
				strBukCol = "PARTIAL_WEEK";
				strLastdayBukCol = "PARTIAL_WEEK_ON_LASTDAY";
			} else if ("M".equals(strBuktype)) {
				strBukCol = "MONTH";
				strLastdayBukCol = "MONTH_ON_LASTDAY";
			} else if ("Y".equals(strBuktype)) {
				strBukCol = "YR";
				strLastdayBukCol = "YR_ON_LASTDAY";
			}
			
			strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				String strCurBukCol = strBukCol;
				
				if (strMeasureNm.indexOf("STOCK") > -1) {
					strCurBukCol = strLastdayBukCol;
				}
				
				if (j > 0) {
					strQuery.append("\t\t                              ");
				}
				
				if ("Y".equals(getComparePlanIdYn())) {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " DECODE(" + getMeasureTbAlias() + ".COMP_PLAN_SEQ, '3', SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strCurBukCol + ", '" + getComparePlanId1() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) - SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strCurBukCol + ", '" + getComparePlanId2() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)), SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strCurBukCol + ", " + getMeasureTbAlias() + ".COMP_PLAN_ID || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0))),\n");
				} else {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " SUM(DECODE(" + getMainTbAlias() + "." + strCurBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)),\n");
				}
			}
			
			strQuery.append("\t\t         NULL) AS " + strTimebuk + "\n");
		}
		
		strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (j > 0) {
				strQuery.append("\t\t                              ");
			}
			
			if (strMeasureNm.indexOf("STOCK") > -1) {
				Map<String, Object> mapBucket = dsBucket.get(dsBucket.size() - 1);
				
				String strLastbuk = mapBucket.get("TIMEBUK") + "";
				
				if ("Y".equals(getComparePlanIdYn())) {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " DECODE(" + getMeasureTbAlias() + ".COMP_PLAN_SEQ, '3', SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + ".DAY, '" + getComparePlanId1() + "' || '" + strLastbuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) - SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + ".DAY, '" + getComparePlanId2() + "' || '" + strLastbuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)), SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + ".DAY, " + getMeasureTbAlias() + ".COMP_PLAN_ID || '" + strLastbuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0))),\n");
				} else {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " SUM(DECODE(" + getMainTbAlias() + ".DAY, '" + strLastbuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)),\n");
				}
			} else {
				if ("Y".equals(getComparePlanIdYn())) {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " DECODE(" + getMeasureTbAlias() + ".COMP_PLAN_SEQ, '3', SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, '" + getComparePlanId1() + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) - SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, '" + getComparePlanId2() + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)), SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, " + getMeasureTbAlias() + ".COMP_PLAN_ID, " + getMainTbAlias() + "." + strMeasureNm + ", 0))),\n");
				} else {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " SUM(" + getMainTbAlias() + "." + strMeasureNm + "),\n");
				}
			}
		}
		
		strQuery.append("\t\t         NULL) AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --qgSelectBucket_FromA_StockOnLastday End\n");
		
		this.qgSelectBucket_FromA_StockOnLastday = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectSizeBucket_FromS_MdsVsPlanning
	 */
	private void setSelectSizeBucket_FromS_MdsVsPlanning(List<Map<String, Object>> dsSizeBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectSizeBucket_FromS_MdsVsPlanning Start\n");
		if (dsSizeBucket != null && dsSizeBucket.size() > 0) {
			for (int i = 0; i < dsSizeBucket.size(); i++) {
				Map<String, Object> mapSizeBucket = dsSizeBucket.get(i);
				
				String strTimebuk = "\"||"+mapSizeBucket.get("SIZE_CD") + "||\"";		// SIZE_CD
				
				strQuery.append("\t\t       , SUM(" + getMainTbAlias() + "." + strTimebuk + ") AS " + strTimebuk + "\n");
			}
			
			strQuery.append("\t\t       , SUM(" + getMainTbAlias() + ".COL_SUM_KEY) AS COL_SUM_KEY\n");
			/*strQuery.append("\t\t       , SUM(" + getMainTbAlias() + ".CHECK_SUM_KEY) AS CHECK_SUM_KEY\n");*/
			strQuery.append("\t\t       --qgSelectSizeBucket_FromS_MdsVsPlanning End\n");
		}
		this.qgSelectSizeBucket_FromS_MdsVsPlanning = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectBucket_FromS
	 */
	private void setSelectBucket_FromS(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromS Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			
			strQuery.append("\t\t       , SUM(" + getMainTbAlias() + "." + strTimebuk + ") AS " + strTimebuk + "\n");
		}
		
		strQuery.append("\t\t       , SUM(" + getMainTbAlias() + ".COL_SUM_KEY) AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --qgSelectBucket_FromS End\n");
		
		this.qgSelectBucket_FromS = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectBucket_FromA_Rmain
	 */
	private void setSelectBucket_FromA_Rmain(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		if ("Y".equals(getComparePlanIdYn())) {
			this.qgSelectBucket_FromA_Rmain = setSelectBucket_FromA_Rmain_Incompare(dsBucket, dsMeasure);
		} else if ( "ForecastShortage".equals(getScrnType())){
			this.qgSelectBucket_FromA_Rmain = setSelectBucket_FromA_Rmain_ForecastShortage(dsBucket, dsMeasure);
		} else {
			this.qgSelectBucket_FromA_Rmain = setSelectBucket_FromA_Rmain_Notcompare(dsBucket, dsMeasure);
		}
	}
	
	private String setSelectBucket_FromA_Rmain_Incompare(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA_Rmain Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			
			strQuery.append("\t\t       , CASE ");
			
			for (int planSeq = 1; planSeq <= 3; planSeq++) {
				
				if (planSeq > 1) {
					strQuery.append("\t\t              ");
				}
				
				strQuery.append("WHEN " + getMeasureTbAlias() + ".COMP_PLAN_SEQ = '" + planSeq + "' THEN CASE ");
				
				for (int j = 0; j < dsMeasure.size(); j++) {
					Map<String, Object> mapMeasure = dsMeasure.get(j);
					
					String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
					
					if (j > 0) {
						strQuery.append("\t\t                                                   ");
					}
					
					int calcIndex = getCalcIndex(strMeasureNm);
					
					if (calcIndex < 0) {
						if (planSeq == 1) {
							strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + ".C1_" + strMeasureNm + "_" + strTimebuk + ")\n");
						} else if (planSeq == 2) {
							strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + ".C2_" + strMeasureNm + "_" + strTimebuk + ")\n");
						} else {
							strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + ".C1_" + strMeasureNm + "_" + strTimebuk + ") - SUM(" + getMainTbAlias() + ".C2_" + strMeasureNm + "_" + strTimebuk + ")\n");
						}
					} else {
						Map<String, Object> map = this.qgCalcField.get(calcIndex);
						
						String source1   = map.get("source1") + "";
						String source2   = map.get("source2") + "";
						String operation = map.get("operation") + "";
						String round     = map.get("round") + "";
						
						String plan1Source1Exp = "SUM(" + getMainTbAlias() + ".C1_" + source1 + "_" + strTimebuk + ")";
						String plan2Source1Exp = "SUM(" + getMainTbAlias() + ".C2_" + source1 + "_" + strTimebuk + ")";
						String plan1Source2Exp = "SUM(" + getMainTbAlias() + ".C1_" + source2 + "_" + strTimebuk + ")";
						String plan2Source2Exp = "SUM(" + getMainTbAlias() + ".C2_" + source2 + "_" + strTimebuk + ")";
						
						strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
						
						if ("PERCENT".equals(operation)) {
							if (planSeq == 1) {
								strQuery.append("DECODE(" + plan1Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan1Source2Exp + " * 100, " + round + "))\n");
							} else if (planSeq == 2) {
								strQuery.append("DECODE(" + plan2Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan2Source2Exp + " * 100, " + round + "))\n");
							} else {
								strQuery.append("DECODE(" + plan1Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan1Source2Exp + " * 100, " + round + "))" + " - " + "DECODE(" + plan2Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan2Source2Exp + " * 100, " + round + "))" + "\n");
							}
						} else if ("PERCENT_DAY".equals(operation)) {
							strQuery.append("DECODE(" + plan1Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan1Source2Exp + ", " + round + "))\n");
						} else if ("PLUS".equals(operation)) {
							if (planSeq == 1) {
								strQuery.append("NVL(" + plan1Source1Exp + ", 0) + NVL(" + plan1Source2Exp + ", 0)\n");
							} else if (planSeq == 2) {
								strQuery.append("NVL(" + plan2Source1Exp + ", 0) + NVL(" + plan2Source2Exp + ", 0)\n");
							} else {
								strQuery.append("(NVL(" + plan1Source1Exp + ", 0) + NVL(" + plan1Source2Exp + ", 0))" + " - " + "(NVL(" + plan2Source1Exp + ", 0) + NVL(" + plan2Source2Exp + ", 0))" + "\n");
							}
						} else if ("MINUS".equals(operation)) {
							if (planSeq == 1) {
								strQuery.append("NVL(" + plan1Source1Exp + ", 0) - NVL(" + plan1Source2Exp + ", 0)\n");
							} else if (planSeq == 2) {
								strQuery.append("NVL(" + plan2Source1Exp + ", 0) - NVL(" + plan2Source2Exp + ", 0)\n");
							} else {
								strQuery.append("(NVL(" + plan1Source1Exp + ", 0) - NVL(" + plan1Source2Exp + ", 0))" + " - " + "(NVL(" + plan2Source1Exp + ", 0) - NVL(" + plan2Source2Exp + ", 0))" + "\n");
							}
						} else {
							strQuery.append("0\n");
						}
					}
				}
				
				strQuery.append("\t\t                                              END\n");
			}
			
			strQuery.append("\t\t         END AS " + strTimebuk + "\n");
		}
		
		strQuery.append("\t\t       , CASE ");
		
		for (int planSeq = 1; planSeq <= 3; planSeq++) {
			
			if (planSeq > 1) {
				strQuery.append("\t\t              ");
			}
			
			strQuery.append("WHEN " + getMeasureTbAlias() + ".COMP_PLAN_SEQ = '" + planSeq + "' THEN CASE ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t                                                   ");
				}
				
				int calcIndex = getCalcIndex(strMeasureNm);
				
				if (calcIndex < 0) {
					if (planSeq == 1) {
						strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + ".C1_" + strMeasureNm + "_COL_SUM_KEY)\n");
					} else if (planSeq == 2) {
						strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + ".C2_" + strMeasureNm + "_COL_SUM_KEY)\n");
					} else {
						strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + ".C1_" + strMeasureNm + "_COL_SUM_KEY) - SUM(" + getMainTbAlias() + ".C2_" + strMeasureNm + "_COL_SUM_KEY)\n");
					}
				} else {
					Map<String, Object> map = this.qgCalcField.get(calcIndex);
					
					String source1   = map.get("source1") + "";
					String source2   = map.get("source2") + "";
					String operation = map.get("operation") + "";
					String round     = map.get("round") + "";
					
					String plan1Source1Exp = "SUM(" + getMainTbAlias() + ".C1_" + source1 + "_COL_SUM_KEY)";
					String plan2Source1Exp = "SUM(" + getMainTbAlias() + ".C2_" + source1 + "_COL_SUM_KEY)";
					String plan1Source2Exp = "SUM(" + getMainTbAlias() + ".C1_" + source2 + "_COL_SUM_KEY)";
					String plan2Source2Exp = "SUM(" + getMainTbAlias() + ".C2_" + source2 + "_COL_SUM_KEY)";
					
					strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
					
					if ("PERCENT".equals(operation)) {
						if (planSeq == 1) {
							strQuery.append("DECODE(" + plan1Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan1Source2Exp + " * 100, " + round + "))\n");
						} else if (planSeq == 2) {
							strQuery.append("DECODE(" + plan2Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan2Source2Exp + " * 100, " + round + "))\n");
						} else {
							strQuery.append("DECODE(" + plan1Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan1Source2Exp + " * 100, " + round + "))" + " - " + "DECODE(" + plan2Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan2Source2Exp + " * 100, " + round + "))" + "\n");
						}
					} else if ("PERCENT_DAY".equals(operation)) {
						strQuery.append("DECODE(" + plan1Source2Exp + ", 0, 0, ROUND(" + plan1Source1Exp + " / " + plan1Source2Exp + ", " + round + "))\n");
					} else if ("PLUS".equals(operation)) {
						if (planSeq == 1) {
							strQuery.append("NVL(" + plan1Source1Exp + ", 0) + NVL(" + plan1Source2Exp + ", 0)\n");
						} else if (planSeq == 2) {
							strQuery.append("NVL(" + plan2Source1Exp + ", 0) + NVL(" + plan2Source2Exp + ", 0)\n");
						} else {
							strQuery.append("(NVL(" + plan1Source1Exp + ", 0) + NVL(" + plan1Source2Exp + ", 0))" + " - " + "(NVL(" + plan2Source1Exp + ", 0) + NVL(" + plan2Source2Exp + ", 0))" + "\n");
						}
					} else if ("MINUS".equals(operation)) {
						if (planSeq == 1) {
							strQuery.append("NVL(" + plan1Source1Exp + ", 0) - NVL(" + plan1Source2Exp + ", 0)\n");
						} else if (planSeq == 2) {
							strQuery.append("NVL(" + plan2Source1Exp + ", 0) - NVL(" + plan2Source2Exp + ", 0)\n");
						} else {
							strQuery.append("(NVL(" + plan1Source1Exp + ", 0) - NVL(" + plan1Source2Exp + ", 0))" + " - " + "(NVL(" + plan2Source1Exp + ", 0) - NVL(" + plan2Source2Exp + ", 0))" + "\n");
						}
					} else {
						strQuery.append("0\n");
					}
				}
			}
			
			strQuery.append("\t\t                                              END\n");
		}
		
		strQuery.append("\t\t         END AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --qgSelectBucket_FromA_Rmain End\n");
		
		return strQuery.toString();
	}
	
	private String setSelectBucket_FromA_Rmain_ForecastShortage(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--setSelectBucket_FromA_Rmain_ForecastShortage Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			
			strQuery.append("\t\t       , CASE ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t              ");
				}
				
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + "." + strMeasureNm + "_" + strTimebuk + ")\n");
			}
			
			strQuery.append("\t\t         END AS " + strTimebuk + "\n");
		}
		
		strQuery.append("\t\t       , CASE ");
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (j > 0) {
				strQuery.append("\t\t              ");
			}
			
			if ("SHORT_E_QTY".equals(strMeasureNm)) {
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN MAX(" + getMainTbAlias() + "." + strMeasureNm + "_COL_SUM_KEY)\n");
			} else if ("SHORT_G_QTY".equals(strMeasureNm)){
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ROUND(DECODE(SUM(A.SHORT_A_QTY_COL_SUM_KEY),0,0,100*SUM(SHORT_B_QTY_COL_SUM_KEY)/SUM(A.SHORT_A_QTY_COL_SUM_KEY)),2)\n");
			} else {
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + "." + strMeasureNm + "_COL_SUM_KEY)\n");
			}
		}
		
		strQuery.append("\t\t         END AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --setSelectBucket_FromA_Rmain_ForecastShortage End\n");
		
		return strQuery.toString();
	}
	
	private String setSelectBucket_FromA_Rmain_Notcompare(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA_Rmain Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			
			strQuery.append("\t\t       , CASE ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t              ");
				}
				
				int calcIndex = getCalcIndex(strMeasureNm);
				
				if (calcIndex < 0) {
					strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + "." + strMeasureNm + "_" + strTimebuk + ")\n");
				} else {
					Map<String, Object> map = this.qgCalcField.get(calcIndex);
					
					String source1   = map.get("source1") + "";
					String source2   = map.get("source2") + "";
					String operation = map.get("operation") + "";
					String round     = map.get("round") + "";
					
					String source1Exp = "SUM(" + getMainTbAlias() + "." + source1 + "_" + strTimebuk + ")";
					String source2Exp = "SUM(" + getMainTbAlias() + "." + source2 + "_" + strTimebuk + ")";
					
					strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
					
					if ("PERCENT".equals(operation)) {
						strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + " * 100, " + round + "))\n");
					} else if ("PERCENT_DAY".equals(operation)) {
						strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + ", " + round + "))\n");
					} else if ("PLUS".equals(operation)) {
						strQuery.append("NVL(" + source1Exp + ", 0) + NVL(" + source2Exp + ", 0)\n");
					} else if ("MINUS".equals(operation)) {
						strQuery.append("NVL(" + source1Exp + ", 0) - NVL(" + source2Exp + ", 0)\n");
					} else {
						strQuery.append("0\n");
					}
				}
			}
			
			strQuery.append("\t\t         END AS " + strTimebuk + "\n");
		}
		
		strQuery.append("\t\t       , CASE ");
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (j > 0) {
				strQuery.append("\t\t              ");
			}
			
			int calcIndex = getCalcIndex(strMeasureNm);
			
			if (calcIndex < 0) {
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + "." + strMeasureNm + "_COL_SUM_KEY)\n");
			} else {
				Map<String, Object> map = this.qgCalcField.get(calcIndex);
				
				String source1   = map.get("source1") + "";
				String source2   = map.get("source2") + "";
				String operation = map.get("operation") + "";
				String round     = map.get("round") + "";
				
				String source1Exp = "SUM(" + getMainTbAlias() + "." + source1 + "_COL_SUM_KEY)";
				String source2Exp = "SUM(" + getMainTbAlias() + "." + source2 + "_COL_SUM_KEY)";
				
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
				
				if ("PERCENT".equals(operation)) {
					strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + " * 100, " + round + "))\n");
				} else if ("PERCENT_DAY".equals(operation)) {
					strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + ", " + round + "))\n");
				} else if ("PLUS".equals(operation)) {
					strQuery.append("NVL(" + source1Exp + ", 0) + NVL(" + source2Exp + ", 0)\n");
				} else if ("MINUS".equals(operation)) {
					strQuery.append("NVL(" + source1Exp + ", 0) - NVL(" + source2Exp + ", 0)\n");
				} else {
					strQuery.append("0\n");
				}
			}
		}
		
		strQuery.append("\t\t         END AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --qgSelectBucket_FromA_Rmain End\n");
		
		return strQuery.toString();
	}
	private void setSelectBucket_FromA_Rmain_Zero(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA_Rmain_Zero Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			
			strQuery.append("\t\t       , CASE ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t              ");
				}
				
				int calcIndex = getCalcIndex(strMeasureNm);
				
				if (calcIndex < 0) {
					strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN DECODE(SUM(" + getMainTbAlias() + "." + strMeasureNm + "_" + strTimebuk + "), 0, NULL, SUM(" + getMainTbAlias() + "." + strMeasureNm + "_" + strTimebuk + "))\n");
				} else {
					Map<String, Object> map = this.qgCalcField.get(calcIndex);
					
					String source1   = map.get("source1") + "";
					String source2   = map.get("source2") + "";
					String operation = map.get("operation") + "";
					String round     = map.get("round") + "";
					
					String source1Exp = "SUM(" + getMainTbAlias() + "." + source1 + "_" + strTimebuk + ")";
					String source2Exp = "SUM(" + getMainTbAlias() + "." + source2 + "_" + strTimebuk + ")";
					
					strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
					
					if ("PERCENT".equals(operation)) {
						strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + " * 100, " + round + "))\n");
					} else if ("PERCENT_DAY".equals(operation)) {
							strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + ", " + round + "))\n");
					} else if ("PLUS".equals(operation)) {
						strQuery.append("NVL(" + source1Exp + ", 0) + NVL(" + source2Exp + ", 0)\n");
					} else if ("MINUS".equals(operation)) {
						strQuery.append("NVL(" + source1Exp + ", 0) - NVL(" + source2Exp + ", 0)\n");
					} else {
						strQuery.append("0\n");
					}
				}
			}
			
			strQuery.append("\t\t         END AS " + strTimebuk + "\n");
		}
		
		strQuery.append("\t\t       , CASE ");
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (j > 0) {
				strQuery.append("\t\t              ");
			}
			
			int calcIndex = getCalcIndex(strMeasureNm);
			
			if (calcIndex < 0) {
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN SUM(" + getMainTbAlias() + "." + strMeasureNm + "_COL_SUM_KEY)\n");
			} else {
				Map<String, Object> map = this.qgCalcField.get(calcIndex);
				
				String source1   = map.get("source1") + "";
				String source2   = map.get("source2") + "";
				String operation = map.get("operation") + "";
				String round     = map.get("round") + "";
				
				String source1Exp = "SUM(" + getMainTbAlias() + "." + source1 + "_COL_SUM_KEY)";
				String source2Exp = "SUM(" + getMainTbAlias() + "." + source2 + "_COL_SUM_KEY)";
				
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
				
				if ("PERCENT".equals(operation)) {
					strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + " * 100, " + round + "))\n");
				} else if ("PERCENT_DAY".equals(operation)) {
						strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + ", " + round + "))\n");
				} else if ("PLUS".equals(operation)) {
					strQuery.append("NVL(" + source1Exp + ", 0) + NVL(" + source2Exp + ", 0)\n");
				} else if ("MINUS".equals(operation)) {
					strQuery.append("NVL(" + source1Exp + ", 0) - NVL(" + source2Exp + ", 0)\n");
				} else {
					strQuery.append("0\n");
				}
			}
		}
		
		strQuery.append("\t\t         END AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --qgSelectBucket_FromA_Rmain_Zero End\n");
		
		this.qgSelectBucket_FromA_Rmain_Zero = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectBucket_FromA_Rsub
	 */
	private void setSelectBucket_FromA_Rsub(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA_Rsub Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			String strBuktype = mapBucket.get("BUKTYPE") + "";		// W, D, ...
			String strBukCol  = "";
			
			if ("D".equals(strBuktype)) {
				strBukCol = "DAY";
			} else if ("W".equals(strBuktype)) {
				strBukCol = "WEEK";
			} else if ("PW".equals(strBuktype)) {
				strBukCol = "PARTIAL_WEEK";
			} else if ("M".equals(strBuktype)) {
				strBukCol = "MONTH";
			} else if ("Y".equals(strBuktype)) {
				strBukCol = "YR";
			}
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (getCalcIndex(strMeasureNm) < 0) {
					if ("Y".equals(getComparePlanIdYn())) {
						strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", '" + getComparePlanId1() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS C1_" + strMeasureNm + "_" + strTimebuk + "\n");
						strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", '" + getComparePlanId2() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS C2_" + strMeasureNm + "_" + strTimebuk + "\n");
					} else {
						strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS " + strMeasureNm + "_" + strTimebuk + "\n");
					}
				}
			}
		}
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (getCalcIndex(strMeasureNm) < 0) {
				if ("Y".equals(getComparePlanIdYn())) {
					strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, '" + getComparePlanId1() + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS C1_" + strMeasureNm + "_COL_SUM_KEY\n");
					strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, '" + getComparePlanId2() + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS C2_" + strMeasureNm + "_COL_SUM_KEY\n");
				} else if ( "ForecastShortage".equals(getScrnType())){
					if ("SHORT_E_QTY".equals(strMeasureNm)) {
						strQuery.append("\t\t       , MAX(" + getMainTbAlias() + "." + strMeasureNm + ") AS " + strMeasureNm + "_COL_SUM_KEY\n");
					} else if ("SHORT_G_QTY".equals(strMeasureNm)){
						strQuery.append("\t\t       , SUM(" + getMainTbAlias() + "." + strMeasureNm + ")/SUM(CASE WHEN " + getMainTbAlias() + "." + strMeasureNm + " IS NULL OR " + getMainTbAlias() + "." + strMeasureNm + " = '' THEN 0 ELSE 1 END) AS " + strMeasureNm + "_COL_SUM_KEY\n");
					} else {
						strQuery.append("\t\t       , SUM(" + getMainTbAlias() + "." + strMeasureNm + ") AS " + strMeasureNm + "_COL_SUM_KEY\n");
					}
				} else {
					strQuery.append("\t\t       , SUM(" + getMainTbAlias() + "." + strMeasureNm + ") AS " + strMeasureNm + "_COL_SUM_KEY\n");
				}
			}
		}
		
		strQuery.append("\t\t       --qgSelectBucket_FromA_Rsub End\n");
		
		this.qgSelectBucket_FromA_Rsub = strQuery.toString();
	}
	/**
	 * [SELECT] Bucket List Setting - qgSelectBucket_FromA_Rsub
	 */
	private void setSelectBucket_FromA_ZeroDel(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA_ZeroDel Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			String strBuktype = mapBucket.get("BUKTYPE") + "";		// W, D, ...
			String strBukCol  = "";
			
			if ("D".equals(strBuktype)) {
				strBukCol = "DAY";
			} else if ("W".equals(strBuktype)) {
				strBukCol = "WEEK";
			} else if ("PW".equals(strBuktype)) {
				strBukCol = "PARTIAL_WEEK";
			} else if ("M".equals(strBuktype)) {
				strBukCol = "MONTH";
			} else if ("Y".equals(strBuktype)) {
				strBukCol = "YR";
			}
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (getCalcIndex(strMeasureNm) < 0) {
					if ("Y".equals(getComparePlanIdYn())) {
						strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", '" + getComparePlanId1() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS C1_" + strMeasureNm + "_" + strTimebuk + "\n");
						strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", '" + getComparePlanId2() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS C2_" + strMeasureNm + "_" + strTimebuk + "\n");
					} else {
						strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS " + strMeasureNm + "_" + strTimebuk + "\n");
					}
				}
			}
		}

		StringBuffer buf = new StringBuffer();
		buf.append(",");
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (getCalcIndex(strMeasureNm) < 0) {
					strQuery.append("\t\t       , SUM(" + getMainTbAlias() + "." + strMeasureNm + ") AS " + strMeasureNm + "_COL_SUM_KEY\n");
					
					buf.append(" SUM(" + getMainTbAlias() + "." + strMeasureNm + ") +"); 
			}
			
		}
		String zeroCheck = buf.toString();

		if(!zeroCheck.equals("")) {
			strQuery.append("\t\t       " + zeroCheck.substring(0, zeroCheck.length()-1) + "AS COL_SUM_CHECK\n");
		}
		strQuery.append("\t\t       --qgSelectBucket_FromA_ZeroDel End\n");
		
		this.qgSelectBucket_FromA_ZeroDel = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectSizeBucket_FromA_Rsub_DeliveryPlan
	 */
	private void setSelectSizeBucket_FromA_Rsub_DeliveryPlan(List<Map<String, Object>> dsSizeBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectSizeBucket_FromA_Rsub_DeliveryPlan Start\n");
		
		if (dsSizeBucket == null || dsSizeBucket.size() == 0) {
			return;
		}
		
		for (int i = 0; i < dsSizeBucket.size(); i++) {
			Map<String, Object> mapSizeBucket = dsSizeBucket.get(i);
			
			String strTimebuk = mapSizeBucket.get("SIZE_CD") + "";		// 01M, 02T, ...
			String strBukCol  = "SIZE_CD";
			
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				strQuery.append("\t\t       , SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) AS " + strMeasureNm + "_" + strTimebuk + "\n");
			}
		}
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			strQuery.append("\t\t       , SUM(" + getMainTbAlias() + "." + strMeasureNm + ") AS " + strMeasureNm + "_COL_SUM_KEY\n");
		}
		
		strQuery.append("\t\t       --qgSelectSizeBucket_FromA_Rsub_DeliveryPlan End\n");
		
		this.qgSelectSizeBucket_FromA_Rsub_DeliveryPlan = strQuery.toString();
	}
	
	private int getCalcIndex(String str) {
		int index = -1;
		
		for (int i = 0; i < this.qgCalcField.size(); i++) {
			Map<String, Object> map = this.qgCalcField.get(i);
			
			String target = map.get("target") + "";
			
			if (str.equals(target)) {
				index = i;
				
				break;
			}
		}
		
		return index;
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectBucket_FromA_CapaAnalysisTfp
	 */
	private void setSelectBucket_FromA_CapaAnalysisTfp(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA_CapaAnalysisTfp Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			String strBuktype = mapBucket.get("BUKTYPE") + "";		// W, D, ...
			String strBukCol  = "";
			
			if ("D".equals(strBuktype)) {
				strBukCol = "DAY";
			} else if ("W".equals(strBuktype)) {
				strBukCol = "WEEK";
			} else if ("PW".equals(strBuktype)) {
				strBukCol = "PARTIAL_WEEK";
			} else if ("M".equals(strBuktype)) {
				strBukCol = "MONTH";
			} else if ("Y".equals(strBuktype)) {
				strBukCol = "YR";
			}
			
//			strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
			strQuery.append("\t\t       , DECODE(A.MEASURE_ID, ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t                              ");
				}
				
				if ("Y".equals(getComparePlanIdYn())) {
					if(strMeasureNm.equals("DIFF_QTY")) {
						strQuery.append(StringUtils.rightPad("'DIFF_QTY',", 20) + " DECODE(A.COMP_PLAN_SEQ, '3', 0, NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", A.COMP_PLAN_ID || '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) - NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", A.COMP_PLAN_ID || '" + strTimebuk + "', " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0)),\n");
					}else if(strMeasureNm.equals("UTILIZATION_RATE")) {
						strQuery.append(StringUtils.rightPad("'UTILIZATION_RATE',", 20) + " DECODE(A.COMP_PLAN_SEQ, '3', 0, CASE WHEN NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", A.COMP_PLAN_ID || '" + strTimebuk + "', " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0) = 0 THEN 100 \n");
						strQuery.append("ELSE ROUND(NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) / NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", A.COMP_PLAN_ID || '" + strTimebuk + "', " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0)*100,1) \n");
						strQuery.append("END),\n");
					}else {
						strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " DECODE(A.COMP_PLAN_SEQ, '3', SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", '" + getComparePlanId1() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) - SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", '" + getComparePlanId2() + "' || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)), SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID || " + getMainTbAlias() + "." + strBukCol + ", A.COMP_PLAN_ID || '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0))),\n");						
					}
				} else {
					if(strMeasureNm.equals("DIFF_QTY")) {
						strQuery.append(StringUtils.rightPad("'DIFF_QTY',", 20) + " NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) - NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0),\n");						
					}else if(strMeasureNm.equals("UTILIZATION_RATE")) {
						strQuery.append(StringUtils.rightPad("'UTILIZATION_RATE',", 20) + " CASE WHEN NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0) = 0 THEN 100 \n");
						strQuery.append("ELSE ROUND(NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) / NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0)*100,1) \n");
						strQuery.append("END,\n");
					}else {
						strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)),0),\n");						
					}
					
				}
			}
			
			strQuery.append("\t\t         NULL) AS " + strTimebuk + "\n");
		}
		
//		strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
		strQuery.append("\t\t       , DECODE(A.MEASURE_ID, ");
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (j > 0) {
				strQuery.append("\t\t                              ");
			}
			
			if ("Y".equals(getComparePlanIdYn())) {
				if(strMeasureNm.equals("DIFF_QTY")) {
					strQuery.append(StringUtils.rightPad("'DIFF_QTY',", 20) + " DECODE(A.COMP_PLAN_SEQ, '3', 0, NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, A.COMP_PLAN_ID, " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) - NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, A.COMP_PLAN_ID, " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0)),\n");
				}else if(strMeasureNm.equals("UTILIZATION_RATE")) {	
					strQuery.append(StringUtils.rightPad("'UTILIZATION_RATE',", 20) + " DECODE(A.COMP_PLAN_SEQ, '3', 0, CASE WHEN NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, A.COMP_PLAN_ID, " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0) = 0 THEN 100 \n");
					strQuery.append("ELSE ROUND(NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, A.COMP_PLAN_ID, " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) / NVL(SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, A.COMP_PLAN_ID, " + getMainTbAlias() + "." + "CAPA_QTY" + ", 0)),0)*100,1) \n");
					strQuery.append("END),\n");
				}else {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " DECODE(A.COMP_PLAN_SEQ, '3', SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, '" + getComparePlanId1() + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)) - SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, '" + getComparePlanId2() + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)), SUM(DECODE(" + getMainTbAlias() + ".PLAN_ID, A.COMP_PLAN_ID, " + getMainTbAlias() + "." + strMeasureNm + ", 0))),\n");
				}
			} else {
				if(strMeasureNm.equals("DIFF_QTY")) {
					strQuery.append(StringUtils.rightPad("'DIFF_QTY',", 20) + " NVL(SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "),0) - NVL(SUM(" + getMainTbAlias() + "." + "CAPA_QTY" + "),0),\n");						
				}else if(strMeasureNm.equals("UTILIZATION_RATE")) {
					strQuery.append(StringUtils.rightPad("'UTILIZATION_RATE',", 20) + " CASE WHEN NVL(SUM(" + getMainTbAlias() + "." + "CAPA_QTY" + "),0) = 0 THEN 100 \n");
					strQuery.append("ELSE ROUND(NVL(SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "),0) / NVL(SUM(" + getMainTbAlias() + "." + "CAPA_QTY" + "),0)*100,1) \n");
					strQuery.append("END,\n");
				}else {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " NVL(SUM(" + getMainTbAlias() + "." + strMeasureNm + "),0),\n");
				}
			}
		}
		
		strQuery.append("\t\t         NULL) AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --qgSelectBucket_FromA_CapaAnalysisTfp End\n");
		
		this.qgSelectBucket_FromA_CapaAnalysisTfp = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectSizeBucket_FromA_MdsVsPlanning
	 */
	private void setSelectSizeBucket_FromA_MdsVsPlanning(List<Map<String, Object>> dsSizeBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectSizeBucket_FromA_MdsVsPlanning Start\n");
		if (dsSizeBucket != null && dsSizeBucket.size() > 0) {
		
			for (int i = 0; i < dsSizeBucket.size(); i++) {
				Map<String, Object> mapSizeBucket = dsSizeBucket.get(i);
				
				String strTimebuk = mapSizeBucket.get("SIZE_CD") + "";		// W201822, D20180601, ...
	
				String strBukCol  = "SIZE_CD";
				
	//			strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
				strQuery.append("\t\t       , DECODE(M.MEASURE_ID, ");
				
				for (int j = 0; j < dsMeasure.size(); j++) {
					Map<String, Object> mapMeasure = dsMeasure.get(j);
					
					String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
					
					if (j > 0) {
						strQuery.append("\t\t                              ");
					}
					
					if(strMeasureNm.equals("DIFF_QTY")) {
						strQuery.append(StringUtils.rightPad("'DIFF_QTY',", 20)  + " ");
//						strQuery.append("\t\t                              ");
//						strQuery.append("	CASE WHEN A.INDE_FLAG = 'I' THEN" + "\n");
//						strQuery.append("\t\t                              ");
						strQuery.append("		NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) - NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_HIST_QTY" + ", 0)),0), \n");
//						strQuery.append("\t\t                              ");						
//						strQuery.append("	ELSE" + "\n");
//						strQuery.append("\t\t                              ");						
//						strQuery.append("		NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_HIST_QTY" + ", 0)),0) - NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) \n");
//						strQuery.append("\t\t                              ");						
//						strQuery.append("	END" + ", \n");
					}else if(strMeasureNm.equals("DIFF_QTY2")) {
						strQuery.append(StringUtils.rightPad("'DIFF_QTY2',", 20)  + " ");
//						strQuery.append("\t\t                              ");
//						strQuery.append("	CASE WHEN A.INDE_FLAG = 'I' THEN" + "\n");
//						strQuery.append("\t\t                              ");
						strQuery.append("		NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "ORDER_QTY" + ", 0)),0) - NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "RELEASE_QTY" + ", 0)),0), \n");
//						strQuery.append("\t\t                              ");						
//						strQuery.append("	ELSE" + "\n");
//						strQuery.append("\t\t                              ");						
//						strQuery.append("		NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_HIST_QTY" + ", 0)),0) - NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + "PLAN_QTY" + ", 0)),0) \n");
//						strQuery.append("\t\t                              ");						
//						strQuery.append("	END" + ", \n");
					}else {
						strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " NVL(SUM(DECODE(" + getMainTbAlias() + "." + strBukCol + ", '" + strTimebuk + "', " + getMainTbAlias() + "." + strMeasureNm + ", 0)),0),\n");						
					}
				}
				
				strQuery.append("\t\t         NULL) AS " + "\"||" +strTimebuk + "||\"" + "\n");
			}
			
	//		strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
			strQuery.append("\t\t       , DECODE(M.MEASURE_ID, ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t                              ");
				}
				
				if(strMeasureNm.equals("DIFF_QTY")) {
					strQuery.append(StringUtils.rightPad("'DIFF_QTY',", 20) + " ");
//					strQuery.append("\t\t                              ");
//					strQuery.append("	CASE WHEN A.INDE_FLAG = 'I' THEN" + "\n");
//					strQuery.append("\t\t                              ");
					strQuery.append("		NVL(SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "),0) - NVL(SUM(" + getMainTbAlias() + "." + "PLAN_HIST_QTY" + "),0), \n");
//					strQuery.append("\t\t                              ");
//					strQuery.append("	ELSE" + "\n");
//					strQuery.append("\t\t                              ");
//					strQuery.append("		NVL(SUM(" + getMainTbAlias() + "." + "PLAN_HIST_QTY" + "),0) - NVL(SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "),0) \n");
//					strQuery.append("\t\t                              ");
//					strQuery.append("	END" + ", \n");
	
				}else if(strMeasureNm.equals("DIFF_QTY2")) {
					strQuery.append(StringUtils.rightPad("'DIFF_QTY2',", 20) + " ");
//					strQuery.append("\t\t                              ");
//					strQuery.append("	CASE WHEN A.INDE_FLAG = 'I' THEN" + "\n");
//					strQuery.append("\t\t                              ");
					strQuery.append("		NVL(SUM(" + getMainTbAlias() + "." + "ORDER_QTY" + "),0) - NVL(SUM(" + getMainTbAlias() + "." + "RELEASE_QTY" + "),0), \n");
//					strQuery.append("\t\t                              ");
//					strQuery.append("	ELSE" + "\n");
//					strQuery.append("\t\t                              ");
//					strQuery.append("		NVL(SUM(" + getMainTbAlias() + "." + "PLAN_HIST_QTY" + "),0) - NVL(SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "),0) \n");
//					strQuery.append("\t\t                              ");
//					strQuery.append("	END" + ", \n");
	
				}else {
					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " NVL(SUM(" + getMainTbAlias() + "." + strMeasureNm + "),0),\n");
				}
			}
			
			strQuery.append("\t\t         NULL) AS COL_SUM_KEY\n");

			/*strQuery.append("\t\t       , DECODE(M.MEASURE_ID, ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t                              ");
				}
				
				strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " NVL(SUM(" + getMainTbAlias() + "." + "PLAN_HIST_QTY" + "),0) - NVL(SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "),0),\n");						
			}
			
			strQuery.append("\t\t         NULL) AS CHECK_SUM_KEY\n");*/
			
			strQuery.append("\t\t       --qgSelectSizeBucket_FromA_MdsVsPlanning End\n");
		} 
		this.qgSelectSizeBucket_FromA_MdsVsPlanning = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectSizeBucket_FromA_Rmain_DeliveryPlan
	 */
	private void setSelectSizeBucket_FromA_Rmain_DeliveryPlan(List<Map<String, Object>> dsSizeBucket, List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectSizeBucket_FromA_Rmain_DeliveryPlan Start\n");
		if (dsSizeBucket != null && dsSizeBucket.size() > 0) {
			
			for (int i = 0; i < dsSizeBucket.size(); i++) {
				Map<String, Object> mapSizeBucket = dsSizeBucket.get(i);
				
				String strTimebuk = mapSizeBucket.get("SIZE_CD") + "";		// 01M, 02T, ...
				
				String strBukCol  = "SIZE_CD";
				
				//			strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
				strQuery.append("\t\t       , DECODE(M.MEASURE_ID, ");
				
				for (int j = 0; j < dsMeasure.size(); j++) {
					Map<String, Object> mapMeasure = dsMeasure.get(j);
					
					String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
					
					if (j > 0) {
						strQuery.append("\t\t                              ");
					}

					strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " NVL(SUM(" + getMainTbAlias() + "." + strMeasureNm + "_" + strTimebuk + "),0),\n");						

				}
				
				strQuery.append("\t\t         NULL) AS " + "\"||" +strTimebuk + "||\"" + "\n");
			}
			
			//		strQuery.append("\t\t       , DECODE(" + getMeasureTbAlias() + ".MEASURE_ID, ");
			strQuery.append("\t\t       , DECODE(M.MEASURE_ID, ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t                              ");
				}

				strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " NVL(SUM(" + getMainTbAlias() + "." + strMeasureNm + "_COL_SUM_KEY),0),\n");

			}
			
			strQuery.append("\t\t         NULL) AS COL_SUM_KEY\n");
			
			/*strQuery.append("\t\t       , DECODE(M.MEASURE_ID, ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t                              ");
				}
				
				strQuery.append(StringUtils.rightPad("'" + strMeasureNm + "',", 20) + " NVL(SUM(" + getMainTbAlias() + "." + "PLAN_HIST_QTY" + "),0) - NVL(SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "),0),\n");						
			}
			
			strQuery.append("\t\t         NULL) AS CHECK_SUM_KEY\n");*/
			
			strQuery.append("\t\t       --qgSelectSizeBucket_FromA_Rmain_DeliveryPlan End\n");
		} 
		this.qgSelectSizeBucket_FromA_Rmain_DeliveryPlan = strQuery.toString();
	}
	
	/**
	 * [SELECT] Bucket List Setting - qgSelectBucket_FromA_MaterialPSI
	 */
	private void setSelectBucket_FromA_MaterialPSI(List<Map<String, Object>> dsBucket, List<Map<String, Object>> dsMeasure) {
StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgSelectBucket_FromA_MaterialPSI Start\n");
		
		for (int i = 0; i < dsBucket.size(); i++) {
			Map<String, Object> mapBucket = dsBucket.get(i);
			
			String strTimebuk = mapBucket.get("TIMEBUK") + "";		// W201822, D20180601, ...
			
			strQuery.append("\t\t       , CASE ");
			
			for (int j = 0; j < dsMeasure.size(); j++) {
				Map<String, Object> mapMeasure = dsMeasure.get(j);
				
				String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
				
				if (j > 0) {
					strQuery.append("\t\t              ");
				}
				
				/*int calcIndex = getCalcIndex(strMeasureNm);*/
				
				/*if (calcIndex < 0) {*/
				
					strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
					
					if(strMeasureNm.equals("PRE_QTY")) {
						strQuery.append("SUM(" + getMainTbAlias() + "." + strMeasureNm + "_" + strTimebuk + ") + SUM(" + getMainTbAlias() + "." + "RTP_QTY" + "_" + strTimebuk + ") - SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "_" + strTimebuk + ")\n");						
					}else {
						strQuery.append("SUM(" + getMainTbAlias() + "." + strMeasureNm + "_" + strTimebuk + ")\n");
					}
					
				/*} else {
					Map<String, Object> map = this.qgCalcField.get(calcIndex);
					
					String source1   = map.get("source1") + "";
					String source2   = map.get("source2") + "";
					String operation = map.get("operation") + "";
					String round     = map.get("round") + "";
					
					String source1Exp = "SUM(" + getMainTbAlias() + "." + source1 + "_" + strTimebuk + ")";
					String source2Exp = "SUM(" + getMainTbAlias() + "." + source2 + "_" + strTimebuk + ")";
					
					strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
					
					if ("PERCENT".equals(operation)) {
						strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + " * 100, " + round + "))\n");
					} else if ("PLUS".equals(operation)) {
						strQuery.append("NVL(" + source1Exp + ", 0) + NVL(" + source2Exp + ", 0)\n");
					} else if ("MINUS".equals(operation)) {
						strQuery.append("NVL(" + source1Exp + ", 0) - NVL(" + source2Exp + ", 0)\n");
					} else {
						strQuery.append("0\n");
					}
				}*/
			}
			
			strQuery.append("\t\t         END AS " + strTimebuk + "\n");
		}
		
		strQuery.append("\t\t       , CASE ");
		
		for (int j = 0; j < dsMeasure.size(); j++) {
			Map<String, Object> mapMeasure = dsMeasure.get(j);
			
			String strMeasureNm = mapMeasure.get("MEASURE_NM") + "";
			
			if (j > 0) {
				strQuery.append("\t\t              ");
			}
			
	/*		int calcIndex = getCalcIndex(strMeasureNm);*/
			
			/*if (calcIndex < 0) {*/
				
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
				
				if(strMeasureNm.equals("PRE_QTY")) {
					strQuery.append("SUM(" + getMainTbAlias() + "." + strMeasureNm + "_COL_SUM_KEY) + SUM(" + getMainTbAlias() + "." + "RTP_QTY" + "_COL_SUM_KEY) - SUM(" + getMainTbAlias() + "." + "PLAN_QTY" + "_COL_SUM_KEY)\n");						
				}else {
					strQuery.append("SUM(" + getMainTbAlias() + "." + strMeasureNm + "_COL_SUM_KEY)\n");
				}
				
				
			/*} else {
				Map<String, Object> map = this.qgCalcField.get(calcIndex);
				
				String source1   = map.get("source1") + "";
				String source2   = map.get("source2") + "";
				String operation = map.get("operation") + "";
				String round     = map.get("round") + "";
				
				String source1Exp = "SUM(" + getMainTbAlias() + "." + source1 + "_COL_SUM_KEY)";
				String source2Exp = "SUM(" + getMainTbAlias() + "." + source2 + "_COL_SUM_KEY)";
				
				strQuery.append("WHEN " + getMeasureTbAlias() + ".MEASURE_ID = '" + strMeasureNm + "' THEN ");
				
				if ("PERCENT".equals(operation)) {
					strQuery.append("DECODE(" + source2Exp + ", 0, 0, ROUND(" + source1Exp + " / " + source2Exp + " * 100, " + round + "))\n");
				} else if ("PLUS".equals(operation)) {
					strQuery.append("NVL(" + source1Exp + ", 0) + NVL(" + source2Exp + ", 0)\n");
				} else if ("MINUS".equals(operation)) {
					strQuery.append("NVL(" + source1Exp + ", 0) - NVL(" + source2Exp + ", 0)\n");
				} else {
					strQuery.append("0\n");
				}
			}*/
		}
		
		strQuery.append("\t\t         END AS COL_SUM_KEY\n");
		strQuery.append("\t\t       --qgSelectBucket_FromA_MaterialPSI End\n");
		
		this.qgSelectBucket_FromA_MaterialPSI = strQuery.toString();
	}
	
	
	/**
	 * [FROM] hrchy Table Setting- qgFrom_HrchyTable
	 */
	private void setFrom_HrchyTable(List<Map<String, Object>> dsDimension) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgFrom_HrchyTable Start\n");
		
		// LOCATION : TBL_MST_DMSN_HRCHY_LOC
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			
			if ("LOCATION".equals(strDmsnTcd)) {
				strQuery.append("\t\t       , CELLOPL.TBL_MST_DMSN_HRCHY_LOC " + strDmsnHrchyTcd + "\n");
				
				break;
			}
		}
		
		// MODEL : TBL_MST_DMSN_HRCHY_MODEL
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			
			if ("MODEL".equals(strDmsnTcd)) {
				strQuery.append("\t\t       , CELLOPL.TBL_MST_DMSN_HRCHY_MODEL " + strDmsnHrchyTcd + "\n");
				
				break;
			}
		}
		
		strQuery.append("\t\t       --qgFrom_HrchyTable End\n");
		
		this.qgFrom_HrchyTable = strQuery.toString();
	}
	
	/**
	 * [FROM] Measure Table Setting - qgFrom_MeasureTable
	 */
	private void setFrom_MeasureTable(List<Map<String, Object>> dsMeasure) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgFrom_MeasureTable Start\n");
		strQuery.append("\t\t       , (\n");
		
		for (int i = 0; i < dsMeasure.size(); i++) {
			Map<String, Object> map = dsMeasure.get(i);
			
			String strMeasureNm     = map.get("MEASURE_NM")      + "";
			String strMeasureDispNm = map.get("MEASURE_DISP_NM") + "";
			String strMeasureSeq    = (i + 1)                    + "";
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t           SELECT '" + getComparePlanId1() + "' AS COMP_PLAN_ID, '1' AS COMP_PLAN_SEQ, '" + strMeasureNm + "' AS MEASURE_ID, '" + strMeasureDispNm + "' AS MEASURE_NM, LPAD('" + strMeasureSeq + "', 2, '0') AS MEASURE_SEQ FROM DUAL UNION ALL\n");
				strQuery.append("\t\t           SELECT '" + getComparePlanId2() + "' AS COMP_PLAN_ID, '2' AS COMP_PLAN_SEQ, '" + strMeasureNm + "' AS MEASURE_ID, '" + strMeasureDispNm + "' AS MEASURE_NM, LPAD('" + strMeasureSeq + "', 2, '0') AS MEASURE_SEQ FROM DUAL UNION ALL\n");
				strQuery.append("\t\t           SELECT 'Diff' AS COMP_PLAN_ID, '3' AS COMP_PLAN_SEQ, '" + strMeasureNm + "' AS MEASURE_ID, '" + strMeasureDispNm + "' AS MEASURE_NM, LPAD('" + strMeasureSeq + "', 2, '0') AS MEASURE_SEQ FROM DUAL");
			} else {
				strQuery.append("\t\t           SELECT '" + strMeasureNm + "' AS MEASURE_ID, '" + strMeasureDispNm + "' AS MEASURE_NM, LPAD('" + strMeasureSeq + "', 2, '0') AS MEASURE_SEQ FROM DUAL");
			}
			
			if (i < dsMeasure.size() - 1) {
				strQuery.append(" UNION ALL\n");
			} else {
				strQuery.append("\n");
			}
		}
		
		strQuery.append("\t\t         ) " + getMeasureTbAlias() + "\n");
		strQuery.append("\t\t       --qgFrom_MeasureTable End\n");
		
		this.qgFrom_MeasureTable = strQuery.toString();
	}
	
	/**
	 * [WHERE] Tree Condition Setting - qgWhere_TreeCond
	 */
	public void setWhere_TreeCond(List<Map<String, Object>> dsTree) {
		this.qgWhere_TreeCond = getWhere_TreeCond(dsTree, false);
	}
	
	/**
	 * [WHERE] Tree Condition Setting - qgWhere_TreeCondLine
	 */
	public void setWhere_TreeCondLine(List<Map<String, Object>> dsTree) {
		this.qgWhere_TreeCondLine = getWhere_TreeCondLine(dsTree, false);
	}
	
	/**
	 * [WHERE] Tree Condition Setting - qgWhere_TreeCondByOriNm
	 */
	public void setWhere_TreeCondByOriNm(List<Map<String, Object>> dsTree) {
		this.qgWhere_TreeCondByOriNm = getWhere_TreeCond(dsTree, true);
	}
	
	public String getWhere_TreeCond(List<Map<String, Object>> dsTree, boolean bOriNm) {
		
		StringBuffer strQuery = new StringBuffer();
		Set<String>   viewSet = new HashSet<String>();
		
		for (int i = 0; i < dsTree.size(); i++) {
			Map<String, Object> map = dsTree.get(i);
			
			String strDmsnId    = map.get("DMSN_ID")    + "";		// L(Location), M(Model)
			String strDmsnLevel = map.get("DMSN_LEVEL") + "";		// 2, 3
			
			viewSet.add(strDmsnId + "," + strDmsnLevel);
		}
		
		strQuery.append("--qgWhere_TreeCond Start\n");
		
		Iterator<String> it = viewSet.iterator();
		
		while (it.hasNext()) {
			String   strViews = (String)it.next();
			String[] strView  = strViews.split(",");
			
			String strDmsnId    = strView[0];
			String strDmsnLevel = strView[1];
			
			int iDmsnLevel = Integer.parseInt(strDmsnLevel);
			
			if (iDmsnLevel < 2) continue;
			
			String strLevel0Id = ("L".equals(strDmsnId) ? codeService.getLssId()  : "ALL");
			String strLevelNm  = ("L".equals(strDmsnId) ? "LOC" : "MODEL");
			
			for (int i = 0; i < iDmsnLevel; i++) {
				if (i == 0) {
					strQuery.append("\t\t   AND ('" + strLevel0Id + "'");
				} else {
					String strMainColNm = strLevelNm + "_LV" + i + "_ID";
					
					if (bOriNm) {
						if ("LOC".equals(strLevelNm)) {
							if (i == 1) {
								strMainColNm = locLevel1ColumnName;
							} else if (i == 2) {
								strMainColNm = locLevel2ColumnName;
							} else if (i == 3) {
								strMainColNm = locLevel3ColumnName;
							}
						} else if ("MODEL".equals(strLevelNm)) {
							if (i == 1) {
								strMainColNm = modelLevel1ColumnName;
							} else if (i == 2) {
								strMainColNm = modelLevel2ColumnName;
							} else if (i == 3) {
								strMainColNm = modelLevel3ColumnName;
							} else if (i == 4) {
								strMainColNm = modelLevel4ColumnName;
							}
						}
					}
					
					strQuery.append(" || '|' || " + getMainTbAlias() + "." + strMainColNm);
				}
			}
			strQuery.append(") IN ( ");
			
			boolean bAdded = false;
			
			for (int i = 0; i < dsTree.size(); i++) {
				Map<String, Object> map = dsTree.get(i);
				
				String strKey    = map.get("DMSN_ID")             + "";		// L(Location), M(Model)
				String strKeyVal = map.get("DMSN_MBR_ID_KEY_VAL") + "";		// VT|ODA, VT|OHS
				
				if (strKey.equals(strDmsnId)) {
					if (bAdded) {
						strQuery.append(", ");
					}
					
					strQuery.append("'" + strKeyVal + "'");
					
					bAdded = true;
				}
			}
			strQuery.append(" )\n");
		}
		
		strQuery.append("\t\t       --qgWhere_TreeCond End\n");
		
		return strQuery.toString();
	}
	
	public String getWhere_TreeCondLine(List<Map<String, Object>> dsTree, boolean bOriNm) {
		
		StringBuffer strQuery = new StringBuffer();
		Set<String>   viewSet = new HashSet<String>();
		
		for (int i = 0; i < dsTree.size(); i++) {
			Map<String, Object> map = dsTree.get(i);
			
			String strDmsnId    = map.get("DMSN_ID")    + "";		// L(Location), M(Model)
			String strDmsnLevel = map.get("DMSN_LEVEL") + "";		// 2, 3
			
			viewSet.add(strDmsnId + "," + strDmsnLevel);
		}
		
		strQuery.append("--qgWhere_TreeCondLine Start\n");
		
		Iterator<String> it = viewSet.iterator();
		
		while (it.hasNext()) {
			String   strViews = (String)it.next();
			String[] strView  = strViews.split(",");
			
			String strDmsnId    = strView[0];
			String strDmsnLevel = strView[1];
			
			int iDmsnLevel = Integer.parseInt(strDmsnLevel);
			
			if (iDmsnLevel < 4) continue;
			
			strQuery.append("\t\t   AND A.LINE_CD");
			
			strQuery.append(" IN ( ");
			boolean bAdded = false;
			for (int i = 0; i < dsTree.size(); i++) {
				Map<String, Object> map = dsTree.get(i);
				
				String dmsnLever    = map.get("DMSN_LEVEL")             + "";		// 
				String dmsnMbrVal    = map.get("DMSN_MBR_VAL")             + "";		// 

				if(dmsnLever.equals("4")) {
					if (bAdded) {
						strQuery.append(", ");
					}
					strQuery.append("'" + dmsnMbrVal + "'");
					bAdded = true;
				}
			}
			strQuery.append(" )\n");
		}
		
		strQuery.append("\t\t       --qgWhere_TreeCondLine End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [WHERE] Hrchy Join Condition Setting - qgWhere_HrchyJoin
	 */
	private void setWhere_HrchyJoin(List<Map<String, Object>> dsAllDimension, List<Map<String, Object>> dsDimension) {
		this.qgWhere_HrchyJoin = getWhere_HrchyJoin(dsAllDimension, dsDimension, false);
	}
	
	/**
	 * [WHERE] Hrchy Join Condition Setting - qgWhere_HrchyJoinByOriNm
	 */
	private void setWhere_HrchyJoinByOriNm(List<Map<String, Object>> dsAllDimension, List<Map<String, Object>> dsDimension) {
		this.qgWhere_HrchyJoinByOriNm = getWhere_HrchyJoin(dsAllDimension, dsDimension, true);
	}
	
	private String getWhere_HrchyJoin(List<Map<String, Object>> dsAllDimension, List<Map<String, Object>> dsDimension, boolean bOriNm) {
		StringBuffer strQuery = new StringBuffer();
		
		Map<String, String> viewMap = new HashMap<String, String>();
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			
			if (!"ATTR".equals(strDmsnTcd)) {
				viewMap.put(strDmsnHrchyTcd,  "Y");
			}
		}
		
		strQuery.append("--qgWhere_HrchyJoin Start\n");
		
		// DMSN_HRCHY_TCD 추가 - 2018.09.17 - Jeong Jong Cheol
		for (String key : viewMap.keySet()) {
			strQuery.append("\t\t   AND " + key + "." + "DMSN_HRCHY_TCD = " + "'" + key + "'" + "\n");
		}
		
		for (int i = 0; i < dsAllDimension.size(); i++) {
			Map<String, Object> map = dsAllDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2,...
			
			if (!"ATTR".equals(strDmsnTcd) && "Y".equals(viewMap.get(strDmsnHrchyTcd))) {
				String strMainColNm = strDimColId + "_ID";
				
				if (bOriNm) {
					if ("LOC_LV1".equals(strDimColId)) {
						strMainColNm = locLevel1ColumnName;
					} else if ("LOC_LV2".equals(strDimColId)) {
						strMainColNm = locLevel2ColumnName;
					} else if ("LOC_LV3".equals(strDimColId)) {
						strMainColNm = locLevel3ColumnName;
					} else if ("MODEL_LV1".equals(strDimColId)) {
						strMainColNm = modelLevel1ColumnName;
					} else if ("MODEL_LV2".equals(strDimColId)) {
						strMainColNm = modelLevel2ColumnName;
					} else if ("MODEL_LV3".equals(strDimColId)) {
						strMainColNm = modelLevel3ColumnName;
					} else if ("MODEL_LV4".equals(strDimColId)) {
						strMainColNm = modelLevel4ColumnName;
					}
				}
				
				strQuery.append("\t\t   AND " + strDmsnHrchyTcd + "." + strDimColId + "_ID = " + getMainTbAlias() + "." + strMainColNm + "\n");
				
				
			}
		}
		
		strQuery.append("\t\t       --qgWhere_HrchyJoin End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromH_All_FromA
	 */
	private void setGroupBy_FromH_All_FromA(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromH_All_FromA = getGroupBy_FromH_All(dsDimension, "FromA");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromH_All_FromM
	 */
	private void setGroupBy_FromH_All_FromM(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromH_All_FromM = getGroupBy_FromH_All(dsDimension, "FromM");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromH_All
	 */
	private void setGroupBy_FromH_All(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromH_All = getGroupBy_FromH_All(dsDimension, "Empty");
	}
	
	private String getGroupBy_FromH_All(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		
		boolean bRollup = false;
		boolean bFirst = true;
		
		strQuery.append("--qgGroupBy_FromH_All_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attribute)
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			String strSumChk       = map.get("SUMCHK")         + "";		// Y/N
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;
			
			String idCol = "";
			String nmCol = "";
			String obCol = "";
			if ("Y".equals(getCompareDmndIdYn()) && (strDimColId.equals("TGAC") || strDimColId.equals("HOLD_YN") || strDimColId.equals("CLOSE_YN"))) {			
			}else {
				if ("ATTR".equals(strDmsnTcd)) {
					idCol = getMainTbAlias() + "." + strDimColId + "_ID";
					nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
					obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
					
				} else {
					idCol = strDmsnHrchyTcd + "." + strDimColId + "_ID";
					nmCol = strDmsnHrchyTcd + "." + strDimColId + "_NM" + getLangEx();
					obCol = strDmsnHrchyTcd + "." + strDimColId + "_ORDB";
				}
				
				if (bRollup) {
					if ("Y".equals(strSumChk)) {
						strQuery.append("),\n\t\t       (" + idCol + ", " + nmCol + ", " + obCol);
					} else {
						strQuery.append(", " + idCol + ", " + nmCol + ", " + obCol);
					}
					
				} else {
					if ("Y".equals(strSumChk)) {
						if (bFirst) {
							strQuery.append("\t\t       ");
							bFirst = false;
						} else {
							strQuery.append("\t\t     , ");
						}
						
						strQuery.append("ROLLUP (\n");
						strQuery.append("\t\t       (" + idCol + ", " + nmCol + ", " + obCol);
						
						bRollup = true;
						
					} else {
						if (bFirst) {
							strQuery.append("\t\t       ");
							bFirst = false;
						} else {
							strQuery.append("\t\t     , ");
						}
						
						strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
					}
				}
			}
		}
		
		if (bRollup) {
			strQuery.append(")\n\t\t       )\n");
		}
		
		if (!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupBy_FromH_All_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromH_All_FromA
	 */
	private void setGroupOnly_FromH_All_FromA(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromH_All_FromA = getGroupOnly_FromH_All(dsDimension, "FromA");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromH_All_FromM
	 */
	private void setGroupOnly_FromH_All_FromM(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromH_All_FromM = getGroupOnly_FromH_All(dsDimension, "FromM");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromH_All
	 */
	private void setGroupOnly_FromH_All(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromH_All = getGroupOnly_FromH_All(dsDimension, "Empty");
	}
	
	private String getGroupOnly_FromH_All(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		boolean bFirst = true;
		
		strQuery.append("--qgGroupOnly_FromH_All_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attribute)
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;
			
			String idCol = "";
			String nmCol = "";
			String obCol = "";
			
			if ("ATTR".equals(strDmsnTcd)) {
				idCol = getMainTbAlias() + "." + strDimColId + "_ID";
				nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
				obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
				
			} else {
				idCol = strDmsnHrchyTcd + "." + strDimColId + "_ID";
				nmCol = strDmsnHrchyTcd + "." + strDimColId + "_NM" + getLangEx();
				obCol = strDmsnHrchyTcd + "." + strDimColId + "_ORDB";
			}
			
			if (bFirst) {
				strQuery.append("\t\t       ");
				bFirst = false;
			} else {
				strQuery.append("\t\t     , ");
			}
			
			strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
		}
		
		if (!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupOnly_FromH_All_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_All_FromA
	 */
	private void setGroupBy_FromA_All_FromA(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_All_FromA = getGroupBy_FromA_All(dsDimension, "FromA");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_All_FromM
	 */
	private void setGroupBy_FromA_All_FromM(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_All_FromM = getGroupBy_FromA_All(dsDimension, "FromM");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_All
	 */
	private void setGroupBy_FromA_All(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_All = getGroupBy_FromA_All(dsDimension, "Empty");
	}
	
	private String getGroupBy_FromA_All(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		
		boolean bRollup = false;
		boolean bFirst = true;
		
		strQuery.append("--qgGroupBy_FromA_All_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strSumChk       = map.get("SUMCHK")         + "";		// Y/N
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;
			
			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			String nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
			String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (bRollup) {
				if ("Y".equals(strSumChk)) {
					strQuery.append("),\n\t\t       (" + idCol + ", " + nmCol + ", " + obCol);
				} else {
					strQuery.append(", " + idCol + ", " + nmCol + ", " + obCol);
				}
				
			} else {
				if ("Y".equals(strSumChk)) {
					if (bFirst) {
						strQuery.append("\t\t       ");
						bFirst = false;
					} else {
						strQuery.append("\t\t     , ");
					}
					
					strQuery.append("ROLLUP (\n");
					strQuery.append("\t\t       (" + idCol + ", " + nmCol + ", " + obCol);
					
					bRollup = true;
					
				} else {
					if (bFirst) {
						strQuery.append("\t\t       ");
						bFirst = false;
					} else {
						strQuery.append("\t\t     , ");
					}
					
					strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
				}
			}
		}
		
		if (bRollup) {
			strQuery.append(")\n\t\t       )\n");
		}
		
		if (!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupBy_FromA_All_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_ID_FromA
	 */
	private void setGroupBy_FromA_ID_FromA(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_ID_FromA = getGroupBy_FromA_ID(dsDimension, "FromA");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_ID_FromM
	 */
	private void setGroupBy_FromA_ID_FromM(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_ID_FromM = getGroupBy_FromA_ID(dsDimension, "FromM");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_ID
	 */
	private void setGroupBy_FromA_ID(List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_ID = getGroupBy_FromA_ID(dsDimension, "Empty");
	}
	
	private String getGroupBy_FromA_ID(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		
		boolean bRollup = false;
		boolean bFirst = true;
		
		strQuery.append("--qgGroupBy_FromA_ID_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strSumChk       = map.get("SUMCHK")         + "";		// Y/N
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			//String nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
			//String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (("MODEL_ID").equals(strDimColId)) {
				idCol += ", " + getMainTbAlias() + "." + strDimColId + "_NM";
			}
			
			if (bRollup) {
				if ("Y".equals(strSumChk)) {
					strQuery.append("),\n\t\t       (" + idCol);
				} else {
					strQuery.append(", " + idCol);
				}
				
			} else {
				if ("Y".equals(strSumChk)) {
					if (bFirst) {
						strQuery.append("\t\t       ");
						bFirst = false;
					} else {
						strQuery.append("\t\t     , ");
					}
					
					strQuery.append("ROLLUP (\n");
					strQuery.append("\t\t       (" + idCol);
					
					bRollup = true;
					
				} else {
					if (bFirst) {
						strQuery.append("\t\t       ");
						bFirst = false;
					} else {
						strQuery.append("\t\t     , ");
					}
					
					strQuery.append(idCol + "\n");
				}
			}
		}
		
		if (bRollup) {
			strQuery.append(")\n\t\t       )\n");
		}
		
		if (!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupBy_FromA_ID_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromA_All_FromA
	 */
	private void setGroupOnly_FromA_All_FromA(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromA_All_FromA = getGroupOnly_FromA_All(dsDimension, "FromA");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromA_All_FromM
	 */
	private void setGroupOnly_FromA_All_FromM(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromA_All_FromM = getGroupOnly_FromA_All(dsDimension, "FromM");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromA_All
	 */
	private void setGroupOnly_FromA_All(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromA_All = getGroupOnly_FromA_All(dsDimension, "Empty");
	}
	
	private String getGroupOnly_FromA_All(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		boolean bFirst = true;
		
		strQuery.append("--qgGroupOnly_FromA_All_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;
			
			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			String nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
			String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (bFirst) {
				strQuery.append("\t\t       ");
				bFirst = false;
			} else {
				strQuery.append("\t\t     , ");
			}
			
			strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
		}
		
		if (!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupOnly_FromA_All_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromA_ID_FromA
	 */
	private void setGroupOnly_FromA_ID_FromA(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromA_ID_FromA = getGroupOnly_FromA_ID(dsDimension, "FromA");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromA_ID_FromM
	 */
	private void setGroupOnly_FromA_ID_FromM(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromA_ID_FromM = getGroupOnly_FromA_ID(dsDimension, "FromM");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromA_ID
	 */
	private void setGroupOnly_FromA_ID(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromA_ID = getGroupOnly_FromA_ID(dsDimension, "Empty");
	}
	
	private String getGroupOnly_FromA_ID(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		boolean bFirst = true;
		
		strQuery.append("--qgGroupOnly_FromA_ID_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			//String nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
			//String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (("MODEL_ID").equals(strDimColId)) {
				idCol += ", " + getMainTbAlias() + "." + strDimColId + "_NM";
			}
			
			if (bFirst) {
				strQuery.append("\t\t       ");
				bFirst = false;
			} else {
				strQuery.append("\t\t     , ");
			}
			
			strQuery.append(idCol + "\n");
		}
		
		if (!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupOnly_FromA_ID_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromS_All
	 */
	private void setGroupOnly_FromS_All(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromS_All = getGroupOnly_FromS_All(dsDimension, "Empty");
	}
	
	private String getGroupOnly_FromS_All(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		boolean bFirst = true;
		
		strQuery.append("--qgGroupOnly_FromS_All_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			String gpCol = getMainTbAlias() + "." + strDimColId + "_GRP";
			String nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
			String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (bFirst) {
				strQuery.append("\t\t       ");
				bFirst = false;
			} else {
				strQuery.append("\t\t     , ");
			}
			
			strQuery.append(idCol + ", " + gpCol + ", " + nmCol + ", " + obCol + "\n");
		}
		
		if (!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupOnly_FromS_All_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupOnly_FromS_ID
	 */
	private void setGroupOnly_FromS_ID(List<Map<String, Object>> dsDimension) {
		this.qgGroupOnly_FromS_ID = getGroupOnly_FromS_ID(dsDimension, "Empty");
	}
	
	private String getGroupOnly_FromS_ID(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		boolean bFirst = true;
		
		strQuery.append("--qgGroupOnly_FromS_ID_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			String gpCol = getMainTbAlias() + "." + strDimColId + "_GRP";
			//String nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
			//String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (bFirst) {
				strQuery.append("\t\t       ");
				bFirst = false;
			} else {
				strQuery.append("\t\t     , ");
			}
			
			strQuery.append(idCol + ", " + gpCol + "\n");
		}
		
		if (!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupOnly_FromS_ID_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_Col_FromA
	 */
	private void setGroupBy_FromA_Col_FromA(List<Map<String, Object>> dsAllDimension, List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_Col_FromA = getGroupBy_FromA_Col(dsAllDimension, dsDimension, "FromA");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_Col_FromM
	 */
	private void setGroupBy_FromA_Col_FromM(List<Map<String, Object>> dsAllDimension, List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_Col_FromM = getGroupBy_FromA_Col(dsAllDimension, dsDimension, "FromM");
	}
	
	/**
	 * [GROUP BY] Group Column Setting - qgGroupBy_FromA_Col
	 */
	private void setGroupBy_FromA_Col(List<Map<String, Object>> dsAllDimension, List<Map<String, Object>> dsDimension) {
		this.qgGroupBy_FromA_Col = getGroupBy_FromA_Col(dsAllDimension, dsDimension, "Empty");
	}
	
	private String getGroupBy_FromA_Col(List<Map<String, Object>> dsAllDimension, List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("--qgGroupBy_FromA_Col_" + from + " Start\n");
		
		boolean bFirst = true;
		
		// Except Attribute
		for (int i = 0; i < dsAllDimension.size(); i++) {
			Map<String, Object> map = dsAllDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			
			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			
			if (!"ATTR".equals(strDmsnTcd)) {
				if (bFirst) {
					strQuery.append("\t\t       ");
					
					bFirst = false;
				} else {
					strQuery.append("\t\t     , ");
				}
				
				strQuery.append(idCol + "\n");
			}
		}
		
		// Attribute
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attribute name](Attribute)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			String nmCol = getMainTbAlias() + "." + strDimColId + "_NM";
			String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if ("ATTR".equals(strDmsnTcd)) {
				if (bFirst) {
					strQuery.append("\t\t       ");
					bFirst = false;
				} else {
					strQuery.append("\t\t     , ");
				}
				
				strQuery.append(idCol + ", " + nmCol + ", " + obCol + "\n");
			}
		}
		
		if(!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_ID\n");
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_ID\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_NM\n");
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgGroupBy_FromA_Col_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromH_All_FromA
	 */
	private void setOrderBy_FromH_All_FromA(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromH_All_FromA = setOrderBy_FromH_All(dsDimension, "FromA");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromH_All_FromM
	 */
	private void setOrderBy_FromH_All_FromM(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromH_All_FromM = setOrderBy_FromH_All(dsDimension, "FromM");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromH_All_FromM
	 */
	private void setOrderBy_FromH_All_Desc_FromM(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromH_All_Desc_FromM = setOrderBy_FromH_All_Desc(dsDimension, "FromM");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromH_All
	 */
	private void setOrderBy_FromH_All(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromH_All = setOrderBy_FromH_All(dsDimension, "Empty");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromH_All_Desc
	 */
	private void setOrderBy_FromH_All_Desc(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromH_All_Desc = setOrderBy_FromH_All_Desc(dsDimension, "Empty");
	}
	
	private String setOrderBy_FromH_All(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		String    sClntCd = codeService.getLssId();
		boolean bFirst = true;
		
		strQuery.append("--qgOrderBy_FromH_All_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attr)
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attr name](Attr)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			if ("Y".equals(getCompareDmndIdYn()) && (strDimColId.equals("TGAC") || strDimColId.equals("HOLD_YN") || strDimColId.equals("CLOSE_YN"))) {			
			}else {
				if (bFirst) {
					strQuery.append("\t\t       ");
					bFirst = false;
				} else {
					strQuery.append("\t\t     , ");
				}
				
				if ("ATTR".equals(strDmsnTcd)) {
					String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
					String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
					
					//strQuery.append("GROUPING(" + idCol + ") DESC, " + obCol + ", " + idCol + "\n");
					if(idCol.equals("A.OGAC_ID")) {
						strQuery.append(obCol + " DESC, " + idCol + " DESC, GROUPING(" + idCol + ") DESC " + "\n");
					}else if(idCol.equals("A.OP_CD_ID")) {
						strQuery.append("GROUPING(" + idCol + ") ASC, " + "( SELECT X.SORT_SQ FROM CELLOPL.TBL_MST_OPERATION X WHERE X.CLNT_CD = '"+sClntCd+"' AND X.OP_CD = A.OP_CD_ID )" + ", " + idCol + "\n");
					}else {
						strQuery.append("GROUPING(" + idCol + ") ASC, " + obCol + ", " + idCol + "\n");	
					}
					
				} else {
					String idCol = strDmsnHrchyTcd + "." + strDimColId + "_ID";
					String obCol = strDmsnHrchyTcd + "." + strDimColId + "_ORDB";
					
//					strQuery.append("GROUPING(" + idCol + ") DESC, " + obCol + ", " + idCol + "\n");
					if(idCol.equals("A.OGAC_ID")) {
						strQuery.append(obCol + " DESC, " + idCol + " DESC, GROUPING(" + idCol + ") DESC " + "\n");
					}else if(idCol.equals("A.OP_CD_ID")) {
						strQuery.append("GROUPING(" + idCol + ") ASC, " + "( SELECT X.SORT_SQ FROM CELLOPL.TBL_MST_OPERATION X WHERE X.CLNT_CD = '"+sClntCd+"' AND X.OP_CD = A.OP_CD_ID )" + ", " + idCol + "\n");						
					}else {
						strQuery.append("GROUPING(" + idCol + ") ASC, " + obCol + ", " + idCol + "\n");
					}
				}
			}
		}
		
		if(!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgOrderBy_FromH_All_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	private String setOrderBy_FromH_All_Desc(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		boolean bFirst = true;
		
		strQuery.append("--qgOrderBy_FromH_All_Desc_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDmsnHrchyTcd = map.get("DMSN_HRCHY_TCD") + "";		// LH1(Location), MH1(Model), ''(Attr)
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attr name](Attr)
			String strDmsnTcd      = map.get("DMSN_TCD")       + "";		// LOCATION, MODEL, ATTR
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			if ("Y".equals(getCompareDmndIdYn()) && (strDimColId.equals("TGAC") || strDimColId.equals("HOLD_YN") || strDimColId.equals("CLOSE_YN"))) {			
			}else {
				if (bFirst) {
					strQuery.append("\t\t       ");
					bFirst = false;
				} else {
					strQuery.append("\t\t     , ");
				}
				
				if ("ATTR".equals(strDmsnTcd)) {
					String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
					String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
					
					//strQuery.append("GROUPING(" + idCol + ") DESC, " + obCol + ", " + idCol + "\n");
					if(idCol.equals("A.OGAC_ID")) {
						strQuery.append(obCol + " DESC, " + idCol + " DESC, GROUPING(" + idCol + ") DESC " + "\n");
					}else {
						strQuery.append("GROUPING(" + idCol + ") DESC, " + obCol + ", " + idCol + "\n");	
					}
					
				} else {
					String idCol = strDmsnHrchyTcd + "." + strDimColId + "_ID";
					String obCol = strDmsnHrchyTcd + "." + strDimColId + "_ORDB";
					
//					strQuery.append("GROUPING(" + idCol + ") DESC, " + obCol + ", " + idCol + "\n");
					if(idCol.equals("A.OGAC_ID")) {
						strQuery.append(obCol + " DESC, " + idCol + " DESC, GROUPING(" + idCol + ") DESC " + "\n");
					}else {
						strQuery.append("GROUPING(" + idCol + ") DESC, " + obCol + ", " + idCol + "\n");
					}
				}
			}
		}
		
		if(!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgOrderBy_FromH_All_Desc_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromA_All_FromA
	 */
	private void setOrderBy_FromA_All_FromA(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromA_All_FromA = setOrderBy_FromA_All(dsDimension, "FromA");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromA_All_FromM
	 */
	private void setOrderBy_FromA_All_FromM(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromA_All_FromM = setOrderBy_FromA_All(dsDimension, "FromM");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromA_All
	 */
	private void setOrderBy_FromA_All(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromA_All = setOrderBy_FromA_All(dsDimension, "Empty");
	}
	
	private String setOrderBy_FromA_All(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		boolean bFirst = true;
		
		strQuery.append("--qgOrderBy_FromA_All_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attr name](Attr)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			String idCol = getMainTbAlias() + "." + strDimColId + "_ID";
			String obCol = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (bFirst) {
				strQuery.append("\t\t       ");
				bFirst = false;
			} else {
				strQuery.append("\t\t     , ");
			}
			
//			strQuery.append("GROUPING(" + idCol + ") DESC, " + obCol + ", " + idCol + "\n");
			strQuery.append("GROUPING(" + idCol + ") ASC, " + obCol + ", " + idCol + "\n");
		}
		
		if(!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgOrderBy_FromA_All_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromS_All_FromA
	 */
	private void setOrderBy_FromS_All_FromA(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromS_All_FromA = setOrderBy_FromS_All(dsDimension, "FromA");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromS_All_FromM
	 */
	private void setOrderBy_FromS_All_FromM(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromS_All_FromM = setOrderBy_FromS_All(dsDimension, "FromM");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromS_All
	 */
	private void setOrderBy_FromS_All(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromS_All = setOrderBy_FromS_All(dsDimension, "Empty");
	}
	
	private String setOrderBy_FromS_All(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		boolean bFirst = true;
		
		strQuery.append("--qgOrderBy_FromS_All_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attr name](Attr)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			String grpCol = getMainTbAlias() + "." + strDimColId + "_GRP";
			String idCol  = getMainTbAlias() + "." + strDimColId + "_ID";
			String obCol  = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (bFirst) {
				strQuery.append("\t\t       ");
				bFirst = false;
			} else {
				strQuery.append("\t\t     , ");
			}
			
			strQuery.append(grpCol + " DESC, " + obCol + ", " + idCol + "\n");
		}
		
		if(!"Empty".equals(from)) {
			String alias = ("FromA".equals(from) ? getMainTbAlias() : getMeasureTbAlias());
			
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgOrderBy_FromS_All_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromS_ID_PlanResult_Tfp
	 */
	private void setOrderBy_FromS_ID_PlanResult_Tfp(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromS_ID_PlanResult_Tfp = setOrderBy_FromS_ID(dsDimension, "PlanResult_Tfp");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromS_ID_FromA
	 */
	private void setOrderBy_FromS_ID_FromA(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromS_ID_FromA = setOrderBy_FromS_ID(dsDimension, "FromA");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromS_ID_FromM
	 */
	private void setOrderBy_FromS_ID_FromM(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromS_ID_FromM = setOrderBy_FromS_ID(dsDimension, "FromM");
	}
	
	/**
	 * [ORDER BY] Order Column Setting - qgOrderBy_FromS_ID
	 */
	private void setOrderBy_FromS_ID(List<Map<String, Object>> dsDimension) {
		this.qgOrderBy_FromS_ID = setOrderBy_FromS_ID(dsDimension, "Empty");
	}
	
	private String setOrderBy_FromS_ID(List<Map<String, Object>> dsDimension, String from) {
		StringBuffer strQuery = new StringBuffer();
		Integer iDimCnt = 0;
		String sClntCd = codeService.getLssId();
		boolean bFirst = true;
		
		strQuery.append("--qgOrderBy_FromS_ID_" + from + " Start\n");
		
		for (int i = 0; i < dsDimension.size(); i++) {
			Map<String, Object> map = dsDimension.get(i);
			
			String strDimColId     = map.get("DIM_COL_ID")     + "";		// LOC_LV1, LOC_LV2, MODEL_LV1, MODEL_LV2, ..., [attr name](Attr)
			String strGrpByYn      = map.get("GROUP_BY_YN")    + "";
			
			if("N".equals(strGrpByYn)) continue;

			String grpCol = getMainTbAlias() + "." + strDimColId + "_GRP";
			String idCol  = getMainTbAlias() + "." + strDimColId + "_ID";
			//String obCol  = getMainTbAlias() + "." + strDimColId + "_ORDB";
			
			if (bFirst) {
				strQuery.append("\t\t       ");
				bFirst = false;
			} else {
				strQuery.append("\t\t     , ");
			}
			
			if ("STYLE_NO".equals(strDimColId) || "PART_CD".equals(strDimColId)) {
				iDimCnt++;
			} 
			
			log.info(">>>>> iDimCnt : " + iDimCnt);
			
			if ("OP_CD".equals(strDimColId)) {
				if(!"FromA".equals(from)) {
					strQuery.append(grpCol + " DESC, " + "( SELECT X.SORT_SQ FROM CELLOPL.TBL_MST_OPERATION X WHERE X.CLNT_CD = '"+sClntCd+"' AND X.OP_CD = A.OP_CD_ID )" + "\n");
				} else {
/* 2022 05 30 OKT planResultTfp 이정우 PAK공정 변경으로 인한 수정
					if ( iDimCnt == 2 ) { 
//						strQuery.append(grpCol + " DESC, " + "( SELECT MAX(OP_SQ) FROM TBL_MST_ROUTING X WHERE X.CLNT_CD = '"+sClntCd+"' AND X.ERP_OP_CD = A.OP_CD_ID AND X.ITEM_CD LIKE A.STYLE_NO_ID||'%'||A.PART_CD_ID ) DESC" + "\n");
//						strQuery.append(grpCol + " DESC, " + "( SELECT MAX(OP_SQ) FROM TBL_MST_OPROUTING X WHERE X.CLNT_CD = '"+sClntCd+"' AND X.ERP_OP_CD = A.OP_CD_ID AND X.ITEM_CD LIKE A.STYLE_NO_ID||'%'||A.PART_CD_ID ) DESC" + "\n");
						strQuery.append(grpCol + " DESC, " + "A.MAX_OP_SQ DESC" + "\n");
					} else {
						strQuery.append(grpCol + " DESC, " + "( SELECT X.SORT_SQ FROM CELLOPL.TBL_MST_OPERATION X WHERE X.CLNT_CD = '"+sClntCd+"' AND X.OP_CD = A.OP_CD_ID )" + "\n");
					}
*/
					strQuery.append(grpCol + " DESC, " + "( SELECT X.SORT_SQ FROM CELLOPL.TBL_MST_OPERATION X WHERE X.CLNT_CD = '"+sClntCd+"' AND X.OP_CD = A.OP_CD_ID )" + "\n");
				}
			} else if ("OP_SQ".equals(strDimColId)) {
				strQuery.append(grpCol + " DESC, TO_NUMBER(" + idCol + ") DESC\n");
			} else if ("LOT_TYPE".equals(strDimColId) && "FromA".equals(from)) {
				strQuery.append(grpCol + " DESC, " + "( SELECT X.CD_ORDB FROM CELLOC.TMDM_CD_CD_TYP_PER_CD X WHERE X.CD_TCD = 'D520' AND X.CD = A.LOT_TYPE_ID )" + "\n");
			} else {
				strQuery.append(grpCol + " DESC, " + idCol + "\n");
			}
		}
		
		if(!"Empty".equals(from)) {
			String alias = "";
			if("FromA".equals(from) || "PlanResult_Tfp".equals(from)) {
				alias = getMainTbAlias();
			}else {
				alias = getMeasureTbAlias();
			}
			if ("Y".equals(getComparePlanIdYn())) {
				strQuery.append("\t\t     , " + alias + ".COMP_PLAN_SEQ\n");
			}
			
			strQuery.append("\t\t     , " + alias + ".MEASURE_SEQ\n");
		}
		
		strQuery.append("\t\t       --qgOrderBy_FromS_ID_" + from + " End\n");
		
		return strQuery.toString();
	}
	
	/**
	 * SQL Pattern A Top
	 */
	private void setPattern_A_Top() {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append(         "SELECT " + getSelectDimension_FromH_All() + "\n");
		strQuery.append("\t\t" + "       " + getSelectMeasure_FromM()       + "\n");
		strQuery.append("\t\t" + "       " + getSelectBucket_FromA()        + "\n");
		strQuery.append("\t\t" + "  FROM ("                                 + "\n");
		
		this.qgPattern_A_Top = strQuery.toString();
	}
	
	/**
	 * SQL Pattern A Bottom
	 */
	private void setPattern_A_Bottom() {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("\t\t" + "       ) A"                             + "\n\n");
		strQuery.append("\t\t" + "       " + getFrom_HrchyTable()         + "\n\n");
		strQuery.append("\t\t" + "       " + getFrom_MeasureTable()       + "\n\n");
		strQuery.append("\t\t" + " WHERE 1 = 1"                           + "\n\n");
		strQuery.append("\t\t" + "       " + getWhere_TreeCond()          + "\n\n");
		strQuery.append("\t\t" + "       " + getWhere_HrchyJoin()         + "\n\n");
		strQuery.append("\t\t" + " GROUP BY"                              + "\n");
		strQuery.append("\t\t" + "       " + getGroupBy_FromH_All_FromM() + "\n\n");
		strQuery.append("\t\t" + " ORDER BY"                              + "\n");
		strQuery.append("\t\t" + "       " + getOrderBy_FromH_All_FromM() + "\n\n");
		
		this.qgPattern_A_Bottom = strQuery.toString();
	}
	
	/**
	 * SQL Pattern B Top
	 */
	private void setPattern_B_Top() {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append(         "SELECT " + getSelectDimension_FromH_All()          + "\n");
		strQuery.append("\t\t" + "       " + getSelectMeasure_FromA()                + "\n");
		strQuery.append("\t\t" + "       " + getSelectBucket_FromS()                 + "\n");
		strQuery.append("\t\t" + "  FROM ("                                          + "\n");
		strQuery.append("\t\t" + "         SELECT " + getSelectDimension_FromA_Col() + "\n");
		strQuery.append("\t\t" + "                " + getSelectMeasure_FromM()       + "\n");
		strQuery.append("\t\t" + "                " + getSelectBucket_FromA()        + "\n");
		strQuery.append("\t\t" + "           FROM ("                                 + "\n");
		
		this.qgPattern_B_Top = strQuery.toString();
	}
	
	/**
	 * SQL Pattern B Bottom
	 */
	private void setPattern_B_Bottom() {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("\t\t" + "                ) A"                             + "\n\n");
		strQuery.append("\t\t" + "                " + getFrom_MeasureTable()       + "\n\n");
		strQuery.append("\t\t" + "          WHERE 1 = 1"                           + "\n\n");
		strQuery.append("\t\t" + "                " + getWhere_TreeCond()          + "\n\n");
		strQuery.append("\t\t" + "          GROUP BY"                              + "\n");
		strQuery.append("\t\t" + "                " + getGroupBy_FromA_Col_FromM() + "\n\n");
		strQuery.append("\t\t" + "       ) A"                                      + "\n\n");
		strQuery.append("\t\t" + "       " + getFrom_HrchyTable()                  + "\n\n");
		strQuery.append("\t\t" + " WHERE 1 = 1"                                    + "\n\n");
		strQuery.append("\t\t" + "       " + getWhere_HrchyJoin()                  + "\n\n");
		strQuery.append("\t\t" + " GROUP BY"                                       + "\n");
		strQuery.append("\t\t" + "       " + getGroupBy_FromH_All_FromA()          + "\n\n");
		strQuery.append("\t\t" + " ORDER BY"                                       + "\n");
		strQuery.append("\t\t" + "       " + getOrderBy_FromH_All_FromA()          + "\n\n");
		
		this.qgPattern_B_Bottom = strQuery.toString();
	}
	
	/**
	 * SQL Pattern C Top
	 */
	private void setPattern_C_Top() {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append(         "SELECT " + getSelectDimension_FromA_All() + "\n");
		strQuery.append("\t\t" + "       " + getSelectMeasure_FromM()       + "\n");
		strQuery.append("\t\t" + "       " + getSelectBucket_FromA()        + "\n");
		strQuery.append("\t\t" + "  FROM ("                                 + "\n");
		
		this.qgPattern_C_Top = strQuery.toString();
	}
	
	/**
	 * SQL Pattern C Bottom
	 */
	private void setPattern_C_Bottom() {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("\t\t" + "       ) A"                             + "\n\n");
		strQuery.append("\t\t" + "       " + getFrom_MeasureTable()       + "\n\n");
		strQuery.append("\t\t" + " WHERE 1 = 1"                           + "\n\n");
		strQuery.append("\t\t" + "       " + getWhere_TreeCond()          + "\n\n");
		strQuery.append("\t\t" + " GROUP BY"                              + "\n");
		strQuery.append("\t\t" + "       " + getGroupBy_FromA_All_FromM() + "\n\n");
		strQuery.append("\t\t" + " ORDER BY"                              + "\n");
		strQuery.append("\t\t" + "       " + getOrderBy_FromA_All_FromM() + "\n\n");
		
		this.qgPattern_C_Bottom = strQuery.toString();
	}
	
	/**
	 * SQL Pattern D Top
	 */
	private void setPattern_D_Top() {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append(         "SELECT " + getSelectDimension_FromH_All()          + "\n");
		strQuery.append("\t\t" + "       " + getSelectMeasure_FromM()                + "\n");
		strQuery.append("\t\t" + "       " + getSelectBucket_FromA_Rmain()            + "\n");
		strQuery.append("\t\t" + "  FROM ("                                          + "\n");
		strQuery.append("\t\t" + "         SELECT " + getSelectDimension_FromA_Col() + "\n");
		strQuery.append("\t\t" + "                " + getSelectBucket_FromA_Rsub()   + "\n");
		strQuery.append("\t\t" + "           FROM ("                                 + "\n");
		
		this.qgPattern_D_Top = strQuery.toString();
	}
	
	/**
	 * SQL Pattern D Bottom
	 */
	private void setPattern_D_Bottom() {
		StringBuffer strQuery = new StringBuffer();
		
		strQuery.append("\t\t" + "                ) A"                             + "\n\n");
		strQuery.append("\t\t" + "          WHERE 1 = 1"                           + "\n\n");
		strQuery.append("\t\t" + "                " + getWhere_TreeCond()          + "\n\n");
		strQuery.append("\t\t" + "          GROUP BY"                              + "\n");
		strQuery.append("\t\t" + "                " + getGroupBy_FromA_Col()       + "\n\n");
		strQuery.append("\t\t" + "       ) A"                                      + "\n\n");
		strQuery.append("\t\t" + "       " + getFrom_HrchyTable()                  + "\n\n");
		strQuery.append("\t\t" + "       " + getFrom_MeasureTable()                + "\n\n");
		strQuery.append("\t\t" + " WHERE 1 = 1"                                    + "\n\n");
		strQuery.append("\t\t" + "       " + getWhere_HrchyJoin()                  + "\n\n");
		strQuery.append("\t\t" + " GROUP BY"                                       + "\n");
		strQuery.append("\t\t" + "       " + getGroupBy_FromH_All_FromM()          + "\n\n");
		strQuery.append("\t\t" + " ORDER BY"                                       + "\n");
		strQuery.append("\t\t" + "       " + getOrderBy_FromH_All_FromM()          + "\n\n");
		
		this.qgPattern_D_Bottom = strQuery.toString();
	}
	
	/**
	 * Getter
	 * @return String
	 */
	public String getLangEx() {
		return this.qgLangEx;
	}
	
	public String getUserRole() {
		return this.qgUserRole;
	}
	
	public String getMainTbAlias() {
		return this.qgMainTbAlias;
	}
	
	public String getMeasureTbAlias() {
		return this.qgMeasureTbAlias;
	}
	
	public String getScrnType() {
		return this.qgScrnType;
	}

	public String getComparePlanIdYn() {
		return this.qgComparePlanIdYn;
	}
	
	public String getComparePlanId1() {
		return this.qgComparePlanId1;
	}
	
	public String getComparePlanId2() {
		return this.qgComparePlanId2;
	}
	
	public String getCompareDmndIdYn() {
		return this.qgCompareDmndIdYn;
	}
	
	public String getCompareDmndId1() {
		return this.qgCompareDmndId1;
	}
	
	public String getCompareDmndId2() {
		return this.qgCompareDmndId2;
	}
	
	public String getCalcField() {
		return this.qgCalcField.toString();
	}
	
	public String getHolidayStr() {
		return this.qgHolidayStr;
	}
	
	public String getSelectDimension_FromH_All() {
		return this.qgSelectDimension_FromH_All;
	}
	
	public String getSelectDimension_FromA_All() {
		return this.qgSelectDimension_FromA_All;
	}
	
	public String getSelectDimension_FromA_ID() {
		return this.qgSelectDimension_FromA_ID;
	}
	
	public String getSelectDimension_FromS_All() {
		return this.qgSelectDimension_FromS_All;
	}
	
	public String getSelectDimension_FromS_ID() {
		return this.qgSelectDimension_FromS_ID;
	}
	
	public String getSelectDimension_FromH_All_Nogrouping() {
		return this.qgSelectDimension_FromH_All_Nogrouping;
	}
	
	public String getSelectDimension_FromA_All_Nogrouping() {
		return this.qgSelectDimension_FromA_All_Nogrouping;
	}
	
	public String getSelectDimension_FromS_All_Nogrouping() {
		return this.qgSelectDimension_FromS_All_Nogrouping;
	}
	
	public String getSelectDimension_FromS_ID_Nogrouping() {
		return this.qgSelectDimension_FromS_ID_Nogrouping;
	}
	
	public String getSelectDimension_FromA_Col() {
		return this.qgSelectDimension_FromA_Col;
	}
	
	public String getSelectMeasure_FromA() {
		return this.qgSelectMeasure_FromA;
	}
	
	public String getSelectMeasure_FromM() {
		return this.qgSelectMeasure_FromM;
	}
	
	public String getSelectBucket_FromA() {
		return this.qgSelectBucket_FromA;
	}
	
	public String getSelectBucket_FromA_StockOnLastday() {
		return this.qgSelectBucket_FromA_StockOnLastday;
	}
	
	public String getSelectSizeBucket_FromA_MdsVsPlanning() {
		return this.qgSelectSizeBucket_FromA_MdsVsPlanning;
	}
	
	public String getSelectSizeBucket_FromA_Rmain_DeliveryPlan() {
		return this.qgSelectSizeBucket_FromA_Rmain_DeliveryPlan;
	}
	
	public String getSelectBucket_FromA_MaterialPSI() {
		return this.qgSelectBucket_FromA_MaterialPSI;
	}
	
	public String getSelectSizeBucket_FromS_MdsVsPlanning() {
		return this.qgSelectSizeBucket_FromS_MdsVsPlanning;
	}
		
	public String getSelectBucket_FromS() {
		return this.qgSelectBucket_FromS;
	}
	
	public String getSelectBucket_FromA_Rmain() {
		return this.qgSelectBucket_FromA_Rmain;
	}
	public String getSelectBucket_FromA_Rmain_Zero() {
		return this.qgSelectBucket_FromA_Rmain_Zero;
	}
	
	public String getSelectBucket_FromA_Rsub() {
		return this.qgSelectBucket_FromA_Rsub;
	}
	
	public String getSelectBucket_FromA_ZeroDel() {
		return this.qgSelectBucket_FromA_ZeroDel;
	}
	
	public String getSelectSizeBucket_FromA_Rsub_DeliveryPlan() {
		return this.qgSelectSizeBucket_FromA_Rsub_DeliveryPlan;
	}
	
	public String getSelectBucket_FromA_CapaAnalysisTfp() {
		return this.qgSelectBucket_FromA_CapaAnalysisTfp;
	}
	
	public String getFrom_HrchyTable() {
		return this.qgFrom_HrchyTable;
	}
	
	public String getFrom_MeasureTable() {
		return this.qgFrom_MeasureTable;
	}
	
	public String getWhere_TreeCond() {
		return this.qgWhere_TreeCond;
	}
	
	public String getWhere_TreeCondLine() {
		return this.qgWhere_TreeCondLine;
	}
	
	public String getWhere_TreeCondByOriNm() {
		return this.qgWhere_TreeCondByOriNm;
	}
	
	public String getWhere_HrchyJoin() {
		return this.qgWhere_HrchyJoin;
	}
	
	public String getWhere_HrchyJoinByOriNm() {
		return this.qgWhere_HrchyJoinByOriNm;
	}
	
	public String getGroupBy_FromH_All_FromA() {
		return this.qgGroupBy_FromH_All_FromA;
	}
	
	public String getGroupBy_FromH_All_FromM() {
		return this.qgGroupBy_FromH_All_FromM;
	}
	
	public String getGroupBy_FromH_All() {
		return this.qgGroupBy_FromH_All;
	}
	
	public String getGroupBy_FromA_All_FromA() {
		return this.qgGroupBy_FromA_All_FromA;
	}
	
	public String getGroupBy_FromA_All_FromM() {
		return this.qgGroupBy_FromA_All_FromM;
	}
	
	public String getGroupBy_FromA_All() {
		return this.qgGroupBy_FromA_All;
	}
	
	public String getGroupBy_FromA_ID_FromA() {
		return this.qgGroupBy_FromA_ID_FromA;
	}
	
	public String getGroupBy_FromA_ID_FromM() {
		return this.qgGroupBy_FromA_ID_FromM;
	}
	
	public String getGroupBy_FromA_ID() {
		return this.qgGroupBy_FromA_ID;
	}
	
	public String getGroupBy_FromA_Col_FromA() {
		return this.qgGroupBy_FromA_Col_FromA;
	}
	
	public String getGroupBy_FromA_Col_FromM() {
		return this.qgGroupBy_FromA_Col_FromM;
	}
	
	public String getGroupBy_FromA_Col() {
		return this.qgGroupBy_FromA_Col;
	}
	
	public String getGroupOnly_FromH_All_FromA() {
		return this.qgGroupOnly_FromH_All_FromA;
	}
	
	public String getGroupOnly_FromH_All_FromM() {
		return this.qgGroupOnly_FromH_All_FromM;
	}
	
	public String getGroupOnly_FromH_All() {
		return this.qgGroupOnly_FromH_All;
	}
	
	public String getGroupOnly_FromA_All_FromA() {
		return this.qgGroupOnly_FromA_All_FromA;
	}
	
	public String getGroupOnly_FromA_All_FromM() {
		return this.qgGroupOnly_FromA_All_FromM;
	}
	
	public String getGroupOnly_FromA_All() {
		return this.qgGroupOnly_FromA_All;
	}
	
	public String getGroupOnly_FromA_ID_FromA() {
		return this.qgGroupOnly_FromA_ID_FromA;
	}
	
	public String getGroupOnly_FromA_ID_FromM() {
		return this.qgGroupOnly_FromA_ID_FromM;
	}
	
	public String getGroupOnly_FromA_ID() {
		return this.qgGroupOnly_FromA_ID;
	}
	
	public String getGroupOnly_FromS_All() {
		return this.qgGroupOnly_FromS_All;
	}
	
	public String getGroupOnly_FromS_ID() {
		return this.qgGroupOnly_FromS_ID;
	}
	
	public String getOrderBy_FromH_All_FromA() {
		return this.qgOrderBy_FromH_All_FromA;
	}
	
	public String getOrderBy_FromH_All_FromM() {
		return this.qgOrderBy_FromH_All_FromM;
	}
	
	public String getOrderBy_FromH_All_Desc_FromM() {
		return this.qgOrderBy_FromH_All_Desc_FromM;
	}
	
	public String getOrderBy_FromH_All() {
		return this.qgOrderBy_FromH_All;
	}
	
	
	public String getOrderBy_FromH_All_Desc() {
		return this.qgOrderBy_FromH_All_Desc;
	}
	
	public String getOrderBy_FromA_All_FromA() {
		return this.qgOrderBy_FromA_All_FromA;
	}
	
	public String getOrderBy_FromA_All_FromM() {
		return this.qgOrderBy_FromA_All_FromM;
	}
	
	public String getOrderBy_FromA_All() {
		return this.qgOrderBy_FromA_All;
	}
	
	public String getOrderBy_FromS_All_FromA() {
		return this.qgOrderBy_FromS_All_FromA;
	}
	
	public String getOrderBy_FromS_All_FromM() {
		return this.qgOrderBy_FromS_All_FromM;
	}
	
	public String getOrderBy_FromS_All() {
		return this.qgOrderBy_FromS_All;
	}
	
	public String getOrderBy_FromS_ID_PlanResult_Tfp() {
		return this.qgOrderBy_FromS_ID_PlanResult_Tfp;
	}
	
	public String getOrderBy_FromS_ID_FromA() {
		return this.qgOrderBy_FromS_ID_FromA;
	}
	
	public String getOrderBy_FromS_ID_FromM() {
		return this.qgOrderBy_FromS_ID_FromM;
	}
	
	public String getOrderBy_FromS_ID() {
		return this.qgOrderBy_FromS_ID;
	}
	
	public String getPattern_A_Top() {
		return this.qgPattern_A_Top;
	}
	
	public String getPattern_A_Bottom() {
		return this.qgPattern_A_Bottom;
	}
	
	public String getPattern_B_Top() {
		return this.qgPattern_B_Top;
	}
	
	public String getPattern_B_Bottom() {
		return this.qgPattern_B_Bottom;
	}
	
	public String getPattern_C_Top() {
		return this.qgPattern_C_Top;
	}
	
	public String getPattern_C_Bottom() {
		return this.qgPattern_C_Bottom;
	}
	
	public String getPattern_D_Top() {
		return this.qgPattern_D_Top;
	}
	
	public String getPattern_D_Bottom() {
		return this.qgPattern_D_Bottom;
	}
}
