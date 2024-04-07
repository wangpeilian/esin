package com.esin.test.common;

import com.esin.base.utility.ListUtil;
import com.esin.base.utility.Logger;
import com.esin.base.utility.MapUtil;
import com.esin.jdbc.dao.Dao;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.query.QueryCriteria;
import com.esin.jdbc.query.SelectBean;
import com.esin.jdbc.query.criterion.Expression;
import com.esin.jdbc.query.criterion.SqlFunc;
import com.esin.jdbc.query.criterion.Query;
import com.esin.test.constants.UserStatus;
import com.esin.test.entity.EDemoA;
import com.esin.test.entity.EDemoB;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainTest {
    public static void main(String[] args) {
        Logger.setLevel(Level.TRACE);
        Dao.setDao(DaoFactoryUtil.createPostgresqlDaoFactory().getDaoHelper());
        Dao.getDao().getDaoFactory().getTableHelper().doMergeTableByEntityClass(EDemoA.class, null, EDemoA.class.getPackage());

        {
            Dao.getDao().createDeleteDestroy(EDemoA.class).execute();
            Dao.getDao().createDeleteDestroy(EDemoB.class).execute();

            EDemoA demoA = new EDemoA();
            demoA.setColString1("a");
            EDemoA demoA2 = new EDemoA(UUID.randomUUID());
            demoA2.setColString2("b");

            demoA = new EDemoA();
            demoA.setId(UUID.randomUUID());
            demoA.setRefA1(new EDemoA(UUID.randomUUID()));
            demoA.setRefA2(demoA);
            demoA.setColInt(1);
            demoA.setColLong(2L);
            demoA.setColFloat(3.4f);
            demoA.setColDouble(5.6d);
            demoA.setColString1("a");
            demoA.setColString2("b");
            demoA.setColString3("c");
            demoA.setColBoolean(Boolean.TRUE);
            demoA.setColDate(new Date());
            demoA.setColEnumOrdinal(UserStatus.Active);
            demoA.setColEnumName(UserStatus.Inactive);
            demoA.setColByte((byte) 7);
            demoA = Dao.getDao().insert(demoA);

            Dao.getDao().clearCache();

            Dao.getDao().createQuery(EDemoA.class).getSelect().addFunc(SqlFunc.Aggregate.Count, SqlFunc.Distinct("refA2.colString2")).getQuery().queryInt();
            Dao.getDao().createQuery(EDemoA.class).getSelect().addFunc(SqlFunc.Aggregate.Avg, SqlFunc.Distinct("colDouble")).getQuery().queryInt();

            List<EDemoA> demoAList = Dao.getDao().list(EDemoA.class, SelectBean.of("refA2.colString2"),
                    Expression.le("refA2.colString2", Dao.getDao().createQuery(EDemoA.class).getSelect().addName("refA2.colString2").getQuery()));

            demoA = Dao.getDao().load(EDemoA.class, demoA.getId());

            demoA.setColString1("aaaaaaaaaaaaaaaa");
            Dao.getDao().update(demoA);

            List<EDemoB> demoBList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                EDemoB demoB = new EDemoB();
                demoB.setId(UUID.randomUUID());
                demoB.setColString1("EDemoB_1_" + i);
                demoB.setColString2("EDemoB_2_" + i);
                demoB.setColString3("EDemoB_3_" + i);
                demoB.setColInt(1);
                demoB.setColLong(1L);
                demoB.setColFloat(1f);
                demoB.setColDouble(1d);
                demoB.setColDate(new Date());
                demoB.setColBoolean(Boolean.TRUE);
                demoB.setColEnumOrdinal(UserStatus.Active);
                demoB.setColEnumName(UserStatus.Inactive);
                demoB.setColByte((byte) 1);
                demoB.setRefA(demoA);
                demoBList.add(demoB);
            }
            Dao.getDao().save(demoBList);

            System.out.println(demoA);

            demoA.getRefA1().setId(demoA.getId());
            Dao.getDao().update(demoA);

            System.out.println(demoA.getRefA1().getId());
            System.out.println(demoA.getRefA1().getColString1());
            System.out.println(demoA.getRefA1().getColString2());
            System.out.println(demoA.getRefA1().getColString3());

            demoA = ListUtil.first(Dao.getDao().createQuery(EDemoA.class).queryLazyEntityList());
            System.out.println(demoA.getListA1().size());
            System.out.println(demoA.getListA2().size());
            System.out.println(demoA.getListB().size());
        }

        {
            QueryCriteria<EDemoB> criteria = new QueryCriteria<>();
            criteria.setQueryJoinColumnList(Arrays.asList("refA.colString2", "refA.colString3"));
            criteria.setEntity(new EDemoB());
            criteria.setFuzzyEntity(new EDemoB());
            criteria.getEntity().setRefA(new EDemoA());
            criteria.getEntity().getRefA().setColInt(1);
//            criteria.getFuzzyEntity().setRefA(new EDemoA());
//            criteria.getFuzzyEntity().getRefA().setColString1("aaa");
            criteria.setFuzzyNameList(Arrays.asList("colString1", "refA.colString2"));
            criteria.setFuzzyText("aa & bb | cc");

            criteria = Dao.getDao().query(criteria);
            System.out.println(criteria.getEntityList());

            Query<EDemoB> query = Dao.getDao().createQuery(EDemoB.class)
                    .getSelect().addEntity().addName(Arrays.asList("refA.colString2", "refA.colString3")).getQuery()
                    .getOrderBy().addName(Arrays.asList("refA.colString1", "refA.colString2")).getQuery()
                    .getLimit().setLimit(1, 10).getQuery();
            List<EDemoB> demoBList = query.getWhere().addCriterion(MapUtil.of(
                    "refA.colInt", 1,
                    "1", Expression.like("refA.colString1", "aaa", SqlFunc.LikeMatch.Any)
            )).getQuery().queryBeanList();
            System.out.println(demoBList);
        }

        {
            Expression expression1 = Expression.like("refA.colString1", "aaa", SqlFunc.LikeMatch.Any);
            Expression expression2 = Expression.ge("colInt", 1);
            Expression expression3 = Expression.eq("colEnumName", UserStatus.Inactive);
            Expression expression4 = Expression.in("colInt", 1, 2, 3);
            Query<EDemoB> query = Dao.getDao().createQuery(EDemoB.class)
                    .getSelect().addEntity().addName("refA.colString2").getQuery()
                    .getFrom().getQuery()
                    .getWhere().addCriterion(expression1, expression2, expression3, expression4).getQuery()
                    .getOrderBy().addName("refA").getQuery()
                    .getLimit().setLimit(0, 2).getQuery();
            List<EDemoB> demoBList = query.queryBeanList();
            System.out.println(demoBList);
        }

        {
            Expression expression1 = Expression.like("refA.colString1", "aaa", SqlFunc.LikeMatch.Any);
            Expression expression2 = Expression.ge("colInt", 1);
            Query<EDemoB> query = Dao.getDao().createQuery(EDemoB.class)
                    .getSelect().addName("refA", IEntity.Col_id).addFunc(SqlFunc.Aggregate.Count, IEntity.Col_id, "c_long").getQuery()
                    .getFrom().getQuery()
                    .getWhere().addCriterion(expression1, expression2).getQuery()
                    .getGroupBy().addName("refA").addName("refA.colString2").getQuery()
                    .getHaving().addCriterion(Expression.ge("count(id)", 2)).getQuery()
                    .getOrderBy().addName("refA").getQuery()
                    .getLimit().setLimit(0, 2).getQuery();
            List<EDemoB> demoBList = query.queryBeanList();
//        List<List<Object>> demoBList = Dao.getDao().query1(ResultConvertFactory.List, query);
            System.out.println(demoBList);
        }

        {
            Dao.getDao().createUpdate(EDemoB.class).
                    setUpdatePropertyMap(MapUtil.of("colInt", "{colInt}+{colInt}+10"))
                    .getWhere().addCriterion(Expression.eq_name("{refA.colInt}+10", "{colInt}+10"))
                    .getQuery().execute();
            Dao.getDao().createUpdate(EDemoA.class).
                    setUpdateValueMap(MapUtil.of("colString2", "12345")).execute();
        }


        Dao.closeConnection();

    }
}
