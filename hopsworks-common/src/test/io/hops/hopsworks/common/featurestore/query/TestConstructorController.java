/*
 * This file is part of Hopsworks
 * Copyright (C) 2020, Logical Clocks AB. All rights reserved
 *
 * Hopsworks is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Hopsworks is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package io.hops.hopsworks.common.featurestore.query;

import io.hops.hopsworks.common.featurestore.FeaturestoreFacade;
import io.hops.hopsworks.common.featurestore.feature.FeatureGroupFeatureDTO;
import io.hops.hopsworks.common.featurestore.featuregroup.FeaturegroupController;
import io.hops.hopsworks.common.featurestore.featuregroup.FeaturegroupDTO;
import io.hops.hopsworks.common.featurestore.featuregroup.FeaturegroupFacade;
import io.hops.hopsworks.common.featurestore.featuregroup.cached.CachedFeaturegroupController;
import io.hops.hopsworks.common.featurestore.online.OnlineFeaturestoreController;
import io.hops.hopsworks.common.featurestore.query.filter.Filter;
import io.hops.hopsworks.common.featurestore.query.filter.FilterController;
import io.hops.hopsworks.common.featurestore.query.filter.FilterLogic;
import io.hops.hopsworks.common.featurestore.query.filter.SqlFilterLogic;
import io.hops.hopsworks.common.featurestore.query.join.Join;
import io.hops.hopsworks.common.featurestore.query.join.JoinController;
import io.hops.hopsworks.common.featurestore.query.join.JoinDTO;
import io.hops.hopsworks.exceptions.FeaturestoreException;
import io.hops.hopsworks.persistence.entity.featurestore.Featurestore;
import io.hops.hopsworks.persistence.entity.featurestore.featuregroup.Featuregroup;
import io.hops.hopsworks.persistence.entity.featurestore.featuregroup.cached.CachedFeaturegroup;
import io.hops.hopsworks.persistence.entity.featurestore.featuregroup.cached.TimeTravelFormat;
import io.hops.hopsworks.persistence.entity.project.Project;
import io.hops.hopsworks.persistence.entity.user.Users;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.SparkSqlDialect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestConstructorController {

  private Featurestore fs;

  private Featuregroup fg1;
  private Featuregroup fg2;
  private Featuregroup fg3;
  private Featuregroup fg4;

  private CachedFeaturegroup cachedFeaturegroup;

  private List<Feature> fg1Features = new ArrayList<>();
  private List<Feature> fg2Features = new ArrayList<>();
  private List<Feature> fg3Features = new ArrayList<>();
  private List<Feature> fg4Features = new ArrayList<>();

  private List<FeatureGroupFeatureDTO> fg1FeaturesDTO = new ArrayList<>();
  private List<FeatureGroupFeatureDTO> fg2FeaturesDTO = new ArrayList<>();
  
  private List<SqlCondition> singleEqualsJoinOperator;

  private FeaturegroupController featuregroupController;
  private FeaturestoreFacade featurestoreFacade;
  private FeaturegroupFacade featuregroupFacade;
  private OnlineFeaturestoreController onlineFeaturestoreController;
  private CachedFeaturegroupController cachedFeaturegroupController;

  private ConstructorController constructorController;
  private FilterController filterController;
  
  private Project project;
  private Users user;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    fs = new Featurestore();
    fs.setHiveDbId(1l);
    fs.setProject(new Project("test_proj"));
    cachedFeaturegroup = new CachedFeaturegroup();
    cachedFeaturegroup.setTimeTravelFormat(TimeTravelFormat.NONE);
    fg1 = new Featuregroup(1);
    fg1.setName("fg1");
    fg1.setVersion(1);
    fg1.setCachedFeaturegroup(cachedFeaturegroup);
    fg1.setFeaturestore(fs);
    fg2 = new Featuregroup(2);
    fg2.setName("fg2");
    fg2.setVersion(1);
    fg2.setCachedFeaturegroup(cachedFeaturegroup);
    fg2.setFeaturestore(fs);
    fg3 = new Featuregroup(3);
    fg3.setName("fg3");
    fg3.setVersion(1);
    fg3.setCachedFeaturegroup(cachedFeaturegroup);
    fg3.setFeaturestore(fs);
    fg4 = new Featuregroup(4);
    fg4.setName("fg4");
    fg4.setVersion(1);
    fg4.setCachedFeaturegroup(cachedFeaturegroup);
    fg4.setFeaturestore(fs);

    fg1Features = new ArrayList<>();
    fg1Features.add(new Feature("pr", "", true));
    fg1Features.add(new Feature("fg1_ft2", "", false));

    fg1FeaturesDTO = new ArrayList<>();
    fg1FeaturesDTO.add(new FeatureGroupFeatureDTO("pr", "Integer", "", true, false, "", null));
    fg1FeaturesDTO.add(new FeatureGroupFeatureDTO("fg1_ft2", "String", "", false, false, "", null));

    fg2Features = new ArrayList<>();
    fg2Features.add(new Feature("pr", "", true));
    fg2Features.add(new Feature("fg2_ft2", "", false));

    fg2FeaturesDTO = new ArrayList<>();
    fg2FeaturesDTO.add(new FeatureGroupFeatureDTO("pr", "Integer", "", true, false, "", null));
    fg2FeaturesDTO.add(new FeatureGroupFeatureDTO("fg2_ft2", "String", "", false, false, "", null));

    fg3Features = new ArrayList<>();
    fg3Features.add(new Feature("fg3_ft1", "", true));
    fg3Features.add(new Feature("fg3_ft2", "", false));

    fg4Features = new ArrayList<>();
    fg4Features.add(new Feature("pr", "fg4", true));
    fg4Features.add(new Feature("fg4_ft4_1", "fg4", "Float", null, "prefix4_"));
    fg4Features.add(new Feature("fg4_ft4_2", "fg4", "Float", null, "prefix4_"));
    fg4Features.add(new Feature("_hoodie_record_key", "fg4", "String", null, null));
    fg4Features.add(new Feature("_hoodie_partition_path", "fg4", "String", null, null));
    fg4Features.add(new Feature("_hoodie_commit_time", "fg4", "String", null, null));
    fg4Features.add(new Feature("_hoodie_file_name", "fg4", "String", null, null));
    fg4Features.add(new Feature("_hoodie_commit_seqno", "fg4", "String", null, null));

    singleEqualsJoinOperator = Arrays.asList(SqlCondition.EQUALS);
    
    featuregroupController = Mockito.mock(FeaturegroupController.class);
    featuregroupFacade = Mockito.mock(FeaturegroupFacade.class);
    featurestoreFacade = Mockito.mock(FeaturestoreFacade.class);
    onlineFeaturestoreController = Mockito.mock(OnlineFeaturestoreController.class);
    cachedFeaturegroupController = Mockito.mock(CachedFeaturegroupController.class);
    project = Mockito.mock(Project.class);
    user = Mockito.mock(Users.class);
    filterController = new FilterController(new ConstructorController());

    constructorController = new ConstructorController(featuregroupController, featurestoreFacade,
        featuregroupFacade, onlineFeaturestoreController, cachedFeaturegroupController, filterController,
      new JoinController(new ConstructorController()));
  }

  @Test
  public void testValidateFeatures() throws Exception {
    List<FeatureGroupFeatureDTO> requestedFeatures = new ArrayList<>();
    requestedFeatures.add(new FeatureGroupFeatureDTO("fg1_ft2"));

    List<Feature> extractedFeatures =
        constructorController.validateFeatures(fg1, requestedFeatures, fg1Features);
    Assert.assertEquals(1, extractedFeatures.size());
    Assert.assertEquals("fg1_ft2", extractedFeatures.get(0).getName());
    // Make sure the object returned is the one for the DB with more infomation in (e.g. Type, Primary key)
    Assert.assertFalse(extractedFeatures.get(0).isPrimary());
  }

  @Test
  public void testMissingFeature() throws Exception {
    List<FeatureGroupFeatureDTO> requestedFeatures = new ArrayList<>();
    requestedFeatures.add(new FeatureGroupFeatureDTO("fg1_ft3"));

    thrown.expect(FeaturestoreException.class);
    constructorController.validateFeatures(fg1, requestedFeatures, fg1Features);
  }

  @Test
  public void testExtractAllFeatures() throws Exception {
    List<FeatureGroupFeatureDTO> requestedFeatures = new ArrayList<>();
    requestedFeatures.add(new FeatureGroupFeatureDTO("*"));

    List<Feature> extractedFeatures =
        constructorController.validateFeatures(fg1, requestedFeatures, fg1Features);
    // Make sure both features have been returned.
    Assert.assertEquals(2, extractedFeatures.size());
  }

  @Test
  public void testExtractFeaturesBothSides() throws Exception {
    Mockito.when(featuregroupController.getFeatures(Mockito.any(), Mockito.any(), Mockito.any()))
      .thenReturn(fg1FeaturesDTO, fg2FeaturesDTO);
    Mockito.when(featuregroupFacade.findById(Mockito.any())).thenReturn(Optional.of(fg1), Optional.of(fg2));
    Mockito.when(featurestoreFacade.getHiveDbName(Mockito.any())).thenReturn("fg1", "fg2");

    FeaturegroupDTO fg1 = new FeaturegroupDTO();
    fg1.setId(1);
    FeaturegroupDTO fg2 = new FeaturegroupDTO();
    fg2.setId(2);

    List<FeatureGroupFeatureDTO> requestedFeatures = new ArrayList<>();
    requestedFeatures.add(new FeatureGroupFeatureDTO("*"));

    QueryDTO rightQueryDTO = new QueryDTO(fg2, requestedFeatures);
    JoinDTO joinDTO = new JoinDTO(rightQueryDTO, null, null);

    QueryDTO queryDTO = new QueryDTO(fg1, requestedFeatures, Arrays.asList(joinDTO));
  
    Map<Integer, String> fgAliasLookup = new HashMap<>();
    Map<Integer, Featuregroup> fgLookup = new HashMap<>();
    Map<Integer, List<Feature>> availableFeatureLookup = new HashMap<>();
  
    constructorController.populateFgLookupTables(queryDTO, 1, fgAliasLookup, fgLookup, availableFeatureLookup,
      project, user, null);
    Query query = constructorController.convertQueryDTO(queryDTO, fgAliasLookup, fgLookup, availableFeatureLookup,
            false);
    
    List<Feature> extractedFeatures = constructorController.collectFeatures(query);
    // Make sure both features have been returned.
    // It's going to be 3 as the feature "pr" will be identified as primary key and joining key
    // so it's not going to be duplicated
    Assert.assertEquals(3, extractedFeatures.size());
    // Make sure the method sets the feature group name
    Assert.assertTrue(extractedFeatures.get(0).getFgAlias(false).equals("fg1") ||
        extractedFeatures.get(0).getFgAlias(false).equals("fg2") );
  }

  @Test
  public void testHandleJoiningKeyRightSide() throws Exception {
    // When specifying the "on" condition we remove the duplicates for the joining condition
    // there was a bug for which if the joining feature was selected only on the right side of the join,
    // it was removed. The joining feature should be removed only if it's present on both side,
    // i.e. it's a duplicate. This test make sure we don't regress.
    Mockito.when(featuregroupController.getFeatures(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(fg1FeaturesDTO, fg2FeaturesDTO);
    Mockito.when(featuregroupFacade.findById(Mockito.any())).thenReturn(Optional.of(fg1), Optional.of(fg2));
    Mockito.when(featurestoreFacade.getHiveDbName(Mockito.any())).thenReturn("fg1", "fg2");

    FeaturegroupDTO fg1 = new FeaturegroupDTO();
    fg1.setId(1);
    FeaturegroupDTO fg2 = new FeaturegroupDTO();
    fg2.setId(2);

    List<FeatureGroupFeatureDTO> leftRequestedFeatures = new ArrayList<>();
    leftRequestedFeatures.add(new FeatureGroupFeatureDTO("fg1_ft2"));

    List<FeatureGroupFeatureDTO> rightRequestedFeatures = new ArrayList<>();
    rightRequestedFeatures.addAll(
        Arrays.asList(new FeatureGroupFeatureDTO("fg2_ft2"), new FeatureGroupFeatureDTO("pr")));

    QueryDTO rightQueryDTO = new QueryDTO(fg2, rightRequestedFeatures);
    JoinDTO joinDTO = new JoinDTO(rightQueryDTO, null, null);

    QueryDTO queryDTO = new QueryDTO(fg1, leftRequestedFeatures, Arrays.asList(joinDTO));

    Map<Integer, String> fgAliasLookup = new HashMap<>();
    Map<Integer, Featuregroup> fgLookup = new HashMap<>();
    Map<Integer, List<Feature>> availableFeatureLookup = new HashMap<>();

    constructorController.populateFgLookupTables(queryDTO, 1, fgAliasLookup, fgLookup, availableFeatureLookup,
        project, user, null);
    Query query = constructorController.convertQueryDTO(queryDTO, fgAliasLookup, fgLookup, availableFeatureLookup,
            false);

    List<Feature> extractedFeatures = constructorController.collectFeatures(query);
    // Make sure both features have been returned.
    // It's going to be 3 as the feature "pr" will be identified as primary key and joining key
    // so it's not going to be duplicated
    Assert.assertEquals(3, extractedFeatures.size());
    // Make sure the method sets the feature group name
    Assert.assertTrue(extractedFeatures.get(0).getFgAlias(false).equals("fg1") ||
        extractedFeatures.get(0).getFgAlias(false).equals("fg2") );
  }

  @Test
  public void testExtractJoinOnMissingFeature() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("fg1_ft2"));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("ft1"));
    availableRight.add(new Feature("fg2_ft2"));

    List<Feature> leftOn = new ArrayList<>();
    leftOn.add(new Feature("ft1"));
  
    List<Feature> rightOn = new ArrayList<>();
    rightOn.add(new Feature("ft1"));

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableRight, availableRight);

    thrown.expect(FeaturestoreException.class);
    constructorController.extractLeftRightOn(leftQuery, rightQuery, leftOn, rightOn, JoinType.INNER, null);
  }

  @Test
  public void testExtractJoinLeftRight() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("fg1_ft3"));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("fg2_ft3"));

    List<Feature> leftOn = Arrays.asList(new Feature("fg1_ft3"));
    List<Feature> rightOn = Arrays.asList(new Feature("fg2_ft3"));
    List<SqlCondition> expectedConditionList = Arrays.asList(SqlCondition.EQUALS);

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Query rightQuery = new Query("fs1","project_fs1", fg2, "fg1", availableRight, availableRight);

    Join join = constructorController.extractLeftRightOn(leftQuery, rightQuery, leftOn, rightOn, JoinType.INNER, null);
    Assert.assertEquals(1, join.getLeftOn().size());
    Assert.assertEquals(1, join.getRightOn().size());
    Assert.assertEquals(expectedConditionList, join.getJoinOperator());
  }

  @Test
  public void testExtractJoinLeftRightWrongSizes() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("fg1_ft3"));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("fg2_ft3"));

    List<Feature> leftOn = Arrays.asList(new Feature("fg1_ft3"), new Feature("additional"));
    List<Feature> rightOn = Arrays.asList(new Feature("fg2_ft3"));

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableRight, availableRight);

    thrown.expect(FeaturestoreException.class);
    constructorController.extractLeftRightOn(leftQuery, rightQuery, leftOn, rightOn, JoinType.INNER, null);
  }

  @Test
  public void testExtractJoinLeftRightMissingFeature() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("fg1_ft3"));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("fg2_ft3"));

    List<Feature> leftOn = Arrays.asList(new Feature("fg1_ft3"));
    List<Feature> rightOn = Arrays.asList(new Feature("fg2_ft1"));

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableRight, availableRight);

    thrown.expect(FeaturestoreException.class);
    constructorController.extractLeftRightOn(leftQuery, rightQuery, leftOn, rightOn, JoinType.INNER, null);
  }

  @Test
  public void testNoJoiningKeySingle() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("ft1", true));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("ft1", true));

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableRight, availableRight);

    List<SqlCondition> expectedConditionList = Arrays.asList(SqlCondition.EQUALS);

    Join join = constructorController.extractPrimaryKeysJoin(leftQuery, rightQuery, JoinType.INNER, null);
    Assert.assertEquals(1, join.getLeftOn().size());
    Assert.assertEquals(1, join.getRightOn().size());
    Assert.assertEquals(expectedConditionList, join.getJoinOperator());
  }

  @Test
  public void testNoJoiningKeyMultipleDifferentSizes() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("ft1", true));
    availableLeft.add(new Feature("ft2", true));
    availableLeft.add(new Feature("ft4", true));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("ft1", true));
    availableRight.add(new Feature("ft2", true));
    availableRight.add(new Feature("ft3", true));

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableRight, availableRight);

    List<SqlCondition> expectedConditionList = Arrays.asList(SqlCondition.EQUALS, SqlCondition.EQUALS);

    Join join = constructorController.extractPrimaryKeysJoin(leftQuery, rightQuery, JoinType.INNER, null);
    Assert.assertEquals(2, join.getLeftOn().size());
    Assert.assertEquals(2, join.getRightOn().size());
    Assert.assertEquals(expectedConditionList, join.getJoinOperator());
  }

  @Test
  public void testNoPrimaryKeys() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("ft1"));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("ft1"));

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableRight, availableRight);

    thrown.expect(FeaturestoreException.class);
    constructorController.extractPrimaryKeysJoin(leftQuery, rightQuery, JoinType.INNER, null);
  }

  @Test
  public void testSingleSideSQLQuery() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("ft1", "fg0", true));

    Query singleSideQuery = new Query("fs1", "project_fs1", fg1, "fg0", availableLeft, availableLeft);
    String query = constructorController.generateSQL(singleSideQuery, false)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg0`.`ft1` FROM `fs1`.`fg1_1` `fg0`", query);
  }

  @Test
  public void testSingleSideSQLQueryOnline() throws Exception {
    ConstructorController constructorController = new ConstructorController();

    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("ft1", "fg0", "Float", null, null));

    Query singleSideQuery = new Query("fs1", "project_fs1", fg1, "fg0", availableLeft, availableLeft);
    String query = constructorController.generateSQL(singleSideQuery, true)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg0`.`ft1` FROM `project_fs1`.`fg1_1` `fg0`", query);
  }

  @Test
  public void testSingleJoinSQLQuery() throws Exception {
    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("ft1","fg0", "Float", null, null));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("ft1","fg1", "Float", null, null));

    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg0", availableRight, availableRight);
    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Join join = new Join(leftQuery, rightQuery, availableLeft, availableLeft, JoinType.INNER, null, singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join));

    String query = constructorController.generateSQL(leftQuery, false)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg0`.`ft1`, `fg1`.`ft1` FROM `fs1`.`fg1_1` `fg1` INNER JOIN " +
        "`fs1`.`fg2_1` `fg0` ON `fg1`.`ft1` = `fg0`.`ft1`", query);
  }

  @Test
  public void testSingleJoinSQLQueryOnline() throws Exception {
    List<Feature> availableLeft = new ArrayList<>();
    availableLeft.add(new Feature("ft1", "fg1", "Float", null, null));

    List<Feature> availableRight = new ArrayList<>();
    availableRight.add(new Feature("ft1", "fg2", "Float", null, null));

    Query leftQuery = new Query("fs1", "project_fs2", fg1, "fg1", availableLeft, availableLeft);
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg2", availableRight, availableRight);

    Join join = new Join(leftQuery, rightQuery, availableLeft, availableLeft, JoinType.INNER, null,
      singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join));

    String query = constructorController.generateSQL(leftQuery, true)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg1`.`ft1`, `fg2`.`ft1` FROM `project_fs2`.`fg1_1` `fg1` INNER JOIN " +
       "`project_fs1`.`fg2_1` `fg2` ON `fg1`.`ft1` = `fg2`.`ft1`", query);
  }

  @Test
  public void testTreeWayJoinSQLNode() throws Exception {
    List<Feature> availableFirst = new ArrayList<>();
    availableFirst.add(new Feature("ft1", "fg0", "Float", null, null));

    List<Feature> availableSecond = new ArrayList<>();
    availableSecond.add(new Feature("ft2", "fg1", "Float", null, null));

    List<Feature> availableThird = new ArrayList<>();
    availableThird.add(new Feature("ft1", "fg2", "Float", null, null));

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg0", availableFirst, availableFirst);
    Query secondQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableSecond , availableSecond);
    Query thirdQuery = new Query("fs1", "project_fs1", fg3,"fg2", availableThird, availableThird);

    Join join = new Join(leftQuery, secondQuery, availableFirst, availableSecond, JoinType.INNER, null, singleEqualsJoinOperator);
    Join secondJoin = new Join(leftQuery, thirdQuery, availableFirst, availableFirst, JoinType.INNER, null,
      singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join, secondJoin));

    String query = constructorController.generateSQL(leftQuery, false)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg0`.`ft1`, `fg1`.`ft2`, `fg2`.`ft1` " +
        "FROM `fs1`.`fg1_1` `fg0` " +
        "INNER JOIN `fs1`.`fg2_1` `fg1` ON `fg0`.`ft1` = `fg1`.`ft2` " +
        "INNER JOIN `fs1`.`fg3_1` `fg2` ON `fg0`.`ft1` = `fg2`.`ft1`", query);
  }
  
  @Test
  public void testTreeWayHudiJoinSQLNode() throws Exception {
    List<Feature> availableFirst = new ArrayList<>();
    availableFirst.add(new Feature("ft1", "fg0", "Float", null, null));
    
    List<Feature> availableSecond = new ArrayList<>();
    availableSecond.add(new Feature("ft2", "fg1", "Float", null, null));
    
    List<Feature> availableThird = new ArrayList<>();
    availableThird.add(new Feature("ft1", "fg2", "Float", null, null));
  
    Mockito.when(cachedFeaturegroupController.dropHudiSpecFeatures(Mockito.any()))
      .thenReturn(availableSecond, availableThird, Stream.of(availableFirst, availableSecond, availableThird)
        .flatMap(Collection::stream).collect(Collectors.toList()));
    
    fg1.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);
    fg2.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);
    fg3.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);
  
    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg0", availableFirst, availableFirst);
    Query secondQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableSecond , availableSecond);
    Query thirdQuery = new Query("fs1", "project_fs1", fg3,"fg2", availableThird, availableThird);
    
    Join join = new Join(leftQuery, secondQuery, availableFirst, availableSecond, JoinType.INNER, null, singleEqualsJoinOperator);
    Join secondJoin = new Join(leftQuery, thirdQuery, availableFirst, availableFirst, JoinType.INNER, null,
      singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join, secondJoin));
    
    String query = constructorController.generateSQL(leftQuery, false)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg0`.`ft1`, `fg1`.`ft2`, `fg2`.`ft1` " +
      "FROM `fg0` `fg0` " +
      "INNER JOIN `fg1` `fg1` ON `fg0`.`ft1` = `fg1`.`ft2` " +
      "INNER JOIN `fg2` `fg2` ON `fg0`.`ft1` = `fg2`.`ft1`", query);
  }
  
  
  @Test
  public void testTreeWayHudiJoinSQLNodeHiveEngine() throws Exception {
    List<Feature> availableFirst = new ArrayList<>();
    availableFirst.add(new Feature("ft1", "fg0", "Float", null, null));
    
    List<Feature> availableSecond = new ArrayList<>();
    availableSecond.add(new Feature("ft2", "fg1", "Float", null, null));
    
    List<Feature> availableThird = new ArrayList<>();
    availableThird.add(new Feature("ft1", "fg2", "Float", null, null));
    
    Mockito.when(cachedFeaturegroupController.dropHudiSpecFeatures(Mockito.any()))
      .thenReturn(availableSecond, availableThird, Stream.of(availableFirst, availableSecond, availableThird)
        .flatMap(Collection::stream).collect(Collectors.toList()));
    
    fg1.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);
    fg2.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);
    fg3.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);
    
    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg0", availableFirst, availableFirst, true);
    Query secondQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableSecond , availableSecond, true);
    Query thirdQuery = new Query("fs1", "project_fs1", fg3,"fg2", availableThird, availableThird, true);
    
    Join join = new Join(leftQuery, secondQuery, availableFirst, availableSecond, JoinType.INNER, null, singleEqualsJoinOperator);
    Join secondJoin = new Join(leftQuery, thirdQuery, availableFirst, availableFirst, JoinType.INNER, null,
      singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join, secondJoin));
    
    String query = constructorController.generateSQL(leftQuery, false)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg0`.`ft1`, `fg1`.`ft2`, `fg2`.`ft1` " +
      "FROM `fs1`.`fg1_1` `fg0` " +
      "INNER JOIN `fs1`.`fg2_1` `fg1` ON `fg0`.`ft1` = `fg1`.`ft2` " +
      "INNER JOIN `fs1`.`fg3_1` `fg2` ON `fg0`.`ft1` = `fg2`.`ft1`", query);
  }
  
  @Test
  public void testThreeWayJoinSQLNodeWithFilters() throws Exception {
    List<Feature> availableFirst = new ArrayList<>();
    availableFirst.add(new Feature("ft1", "fg0", "Float", null, null));
  
    List<Feature> availableSecond = new ArrayList<>();
    availableSecond.add(new Feature("ft2", "fg1", "Float", null, null));
  
    List<Feature> availableThird = new ArrayList<>();
    availableThird.add(new Feature("ft1", "fg2", "Float", null, null));
    availableThird.add(new Feature("ft2", "fg2", "Float", null, null));
  
    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg0", availableFirst, availableFirst);
    Query secondQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableSecond , availableSecond);
    Query thirdQuery = new Query("fs1", "project_fs1", fg3,"fg2", availableThird, availableThird);
  
    FilterLogic firstFilter = new FilterLogic(SqlFilterLogic.AND);
    firstFilter.setLeftFilter(new Filter(availableFirst.get(0), SqlCondition.EQUALS, "10"));
    FilterLogic rightLogic = new FilterLogic(SqlFilterLogic.OR);
    rightLogic.setLeftFilter(new Filter(availableThird.get(0), SqlCondition.EQUALS, "10"));
    rightLogic.setRightFilter(new Filter(availableThird.get(1), SqlCondition.EQUALS, "10"));
    firstFilter.setRightLogic(rightLogic);
    leftQuery.setFilter(firstFilter);
  
    FilterLogic secondFilter = new FilterLogic(SqlFilterLogic.SINGLE);
    secondFilter.setLeftFilter(new Filter(availableSecond.get(0), SqlCondition.NOT_EQUALS, "10"));
    secondQuery.setFilter(secondFilter);
    
    Join join = new Join(leftQuery, secondQuery, availableFirst, availableSecond, JoinType.INNER, null, singleEqualsJoinOperator);
    Join secondJoin = new Join(leftQuery, thirdQuery, availableFirst, availableFirst, JoinType.INNER, null,
      singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join, secondJoin));
  
    String query = constructorController.generateSQL(leftQuery, false)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg0`.`ft1`, `fg1`.`ft2`, `fg2`.`ft1`, `fg2`.`ft2` " +
      "FROM `fs1`.`fg1_1` `fg0` " +
      "INNER JOIN `fs1`.`fg2_1` `fg1` ON `fg0`.`ft1` = `fg1`.`ft2` " +
      "INNER JOIN `fs1`.`fg3_1` `fg2` ON `fg0`.`ft1` = `fg2`.`ft1` " +
      "WHERE `fg0`.`ft1` = 10 AND (`fg2`.`ft1` = 10 OR `fg2`.`ft2` = 10) AND `fg1`.`ft2` <> 10", query);
  }

  @Test
  public void testCaseWhenDefaultStringSpark() {
    Feature feature =
      new Feature("feature", "fg", "String", false, "hello", null);
    String output = constructorController.caseWhenDefault(feature)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql();
    String expected = "CASE WHEN `fg`.`feature` IS NULL THEN 'hello' ELSE `fg`.`feature` END";
    Assert.assertEquals(expected, output);
  }

  @Test
  public void testCaseWhenDefaultOtherHive() {
    Feature feature =
      new Feature("feature", "fg", "Float", false, "10.0", null);
    String output = constructorController.caseWhenDefault(feature)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql();
    String expected = "CASE WHEN `fg`.`feature` IS NULL THEN 10.0 ELSE `fg`.`feature` END";
    Assert.assertEquals(expected, output);
  }

  @Test
  public void testSelectWithDefaultAsSpark() {
    Feature feature =
      new Feature("feature", "fg", "Float", false, "10.0", null);
    String output = constructorController.selectWithDefaultAs(feature)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql();
    String expected = "CASE WHEN `fg`.`feature` IS NULL THEN 10.0 ELSE `fg`.`feature` END `feature`";
    Assert.assertEquals(expected, output);
  }

  @Test
  public void testSelectWithDefaultAsHive() {
    Feature feature =
      new Feature("feature", "fg", "Float", false, "10.0", null);
    String output = constructorController.selectWithDefaultAs(feature)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql();
    String expected = "CASE WHEN `fg`.`feature` IS NULL THEN 10.0 ELSE `fg`.`feature` END `feature`";
    Assert.assertEquals(expected, output);
  }

  @Test
  public void testPrefixFeatureJoins() throws Exception {

    List<Feature> availableFirst = new ArrayList<>();
    availableFirst.add(new Feature("ft1", "fg0", "Float", null, null));

    List<Feature> availableSecond = new ArrayList<>();
    availableSecond.add(new Feature("ft2", "fg1", "Float", null, "prefix2_"));

    List<Feature> availableThird = new ArrayList<>();
    availableThird.add(new Feature("fg4_ft4_1", "fg2", "Float", null, "prefix4_"));
    availableThird.add(new Feature("fg4_ft4_2", "fg2", "Float", null, "prefix4_"));

    Mockito.when(cachedFeaturegroupController.dropHudiSpecFeatures(Mockito.any()))
        .thenReturn(availableSecond, availableThird, Stream.of(availableFirst, availableSecond, availableThird)
            .flatMap(Collection::stream).collect(Collectors.toList()));

    fg1.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);
    fg2.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);
    fg4.getCachedFeaturegroup().setTimeTravelFormat(TimeTravelFormat.HUDI);

    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg0", availableFirst, availableFirst);
    Query secondQuery = new Query("fs1", "project_fs1", fg2, "fg1", availableSecond , availableSecond);
    Query thirdQuery = new Query("fs1", "project_fs1", fg4,"fg2", fg4Features, fg4Features);

    Join join = new Join(leftQuery, secondQuery, availableFirst, availableSecond, JoinType.INNER, "prefix2_", singleEqualsJoinOperator);
    Join secondJoin = new Join(leftQuery, thirdQuery, availableFirst, availableFirst, JoinType.INNER, "prefix4_",
      singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join, secondJoin));

    String query = constructorController.generateSQL(leftQuery, true)
      .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql().replace("\n", " ");
    Assert.assertEquals("SELECT `fg0`.`ft1`, `fg1`.`ft2` `prefix2_ft2`, " +
            "`fg2`.`fg4_ft4_1` `prefix4_fg4_ft4_1`, `fg2`.`fg4_ft4_2` `prefix4_fg4_ft4_2` " +
            "FROM `project_fs1`.`fg1_1` `fg0` INNER JOIN `project_fs1`.`fg2_1` `fg1` ON `fg0`.`ft1` = `fg1`.`ft2` " +
            "INNER JOIN `project_fs1`.`fg4_1` `fg2` ON `fg0`.`ft1` = `fg2`.`ft1`",
        query);
  }
  
  @Test
  public void testRemoveDuplicateColumns() throws Exception {
    List<Feature> joinLeft = new ArrayList<>();
    joinLeft.add(new Feature("ft1","fg0", "Float", null, null));
    
    List<Feature> availableLeft = new ArrayList<>(joinLeft);
    availableLeft.add(new Feature("ft2", "fg0", "int", null, null));
  
    List<Feature> joinRight = new ArrayList<>();
    joinRight.add(new Feature("ft1","fg1", "Float", null, null));
    
    List<Feature> availableRight = new ArrayList<>(joinRight);
    availableRight.add(new Feature("ft2", "fg1", "int", null, null));
    availableRight.add(new Feature("ft3", "fg1", "int", null, null));
  
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg0", availableRight, availableRight);
    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Join join = new Join(leftQuery, rightQuery, joinLeft, joinRight, JoinType.INNER, null, singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join));
    
    constructorController.removeDuplicateColumns(leftQuery, false);
    Assert.assertEquals(2, rightQuery.getFeatures().size());
    Assert.assertEquals("ft2", rightQuery.getFeatures().get(0).getName());
    Assert.assertEquals("ft3", rightQuery.getFeatures().get(1).getName());
  }

  @Test
  public void testRemoveDuplicateColumnsPrefix() throws Exception {
    List<Feature> joinLeft = new ArrayList<>();
    joinLeft.add(new Feature("ft1","fg0", "Float", null, null));
  
    List<Feature> availableLeft = new ArrayList<>(joinLeft);
    availableLeft.add(new Feature("ft2", "fg0", "int", null, null));
  
    List<Feature> joinRight = new ArrayList<>();
    joinRight.add(new Feature("ft1","fg1", "Float", null, "right_"));
  
    List<Feature> availableRight = new ArrayList<>(joinRight);
    availableRight.add(new Feature("ft2", "fg1", "int", null, "right_"));
    availableRight.add(new Feature("ft3", "fg1", "int", null, "right_"));
  
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg0", availableRight, availableRight);
    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Join join = new Join(leftQuery, rightQuery, joinLeft, joinRight, JoinType.INNER, "right_",
      singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join));
    
    constructorController.removeDuplicateColumns(leftQuery, false);
    Assert.assertEquals(3, rightQuery.getFeatures().size());
  }

  @Test
  public void testRemoveDuplicateColumnsPitEnabled() throws Exception {
    List<Feature> joinLeft = new ArrayList<>();
    joinLeft.add(new Feature("ft1","fg0", "Float", null, null));
  
    List<Feature> availableLeft = new ArrayList<>(joinLeft);
    availableLeft.add(new Feature("ft2", "fg0", "int", null, null));
  
    List<Feature> joinRight = new ArrayList<>();
    joinRight.add(new Feature("ft1","fg1", "Float", null, "right_"));
  
    List<Feature> availableRight = new ArrayList<>(joinRight);
    availableRight.add(new Feature("ft2", "fg1", "int", null, "right_"));
    availableRight.add(new Feature("ft3", "fg1", "int", null, "right_"));
    
    fg2.setEventTime("ft3");
  
    Query rightQuery = new Query("fs1", "project_fs1", fg2, "fg0", availableRight, availableRight);
    Query leftQuery = new Query("fs1", "project_fs1", fg1, "fg1", availableLeft, availableLeft);
    Join join = new Join(leftQuery, rightQuery, joinLeft, joinRight, JoinType.INNER, "right_",
      singleEqualsJoinOperator);
    leftQuery.setJoins(Arrays.asList(join));
  
    constructorController.removeDuplicateColumns(leftQuery, true);
    Assert.assertEquals(2, rightQuery.getFeatures().size());
    Assert.assertEquals("ft1", rightQuery.getFeatures().get(0).getName());
    Assert.assertEquals("ft2", rightQuery.getFeatures().get(1).getName());
  }
  
  @Test
  public void testGetWithOrWithoutPrefix() throws Exception {
    Feature feature = new Feature("ft1", "fg1", "int", null, null);
    Assert.assertEquals("`fg1`.`ft1`",
      constructorController.getWithOrWithoutPrefix(feature)
        .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql());
  }

  @Test
  public void testGetWithOrWithoutPrefixWithPrefix() throws Exception {
    Feature feature = new Feature("ft1", "fg1", "int", null, "right_");
    Assert.assertEquals("`fg1`.`ft1` `right_ft1`",
      constructorController.getWithOrWithoutPrefix(feature)
        .toSqlString(new SparkSqlDialect(SqlDialect.EMPTY_CONTEXT)).getSql());
  }
}
