#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Pedestrian Recognition/classification based on RandomForest.

</p>
@author: CaiZiHao
"""

import logging
import time
import pandas as pd
from sklearn import ensemble
import numpy as np
from sklearn import ensemble
from sklearn import metrics
from sklearn.decomposition import PCA
from sklearn import preprocessing
from sklearn import tree
from sklearn import svm
from sklearn.neighbors import KNeighborsClassifier
from sklearn import model_selection      
from pandas import DataFrame 
from pandas.core.series import Series
# Base dataset directory.
parent_path = "./PR_dataset/"
# Train and Test file path.
train_dir = parent_path + "data_train_sample.csv"
test_dir = parent_path + "data_test_sample.csv"
# Field prefix.
field_prefix = "D"
# Data dimension.
feature_dimension = 2330

# Log directory.
log_dir = "./log/5-30/"


myLOGGER = logging.getLogger('myPRModelLogger')

# Log handler.
def logger_custom(default_log_name='test.log', file_mode='w', file_log_level=logging.DEBUG):
    myLOGGER.setLevel(logging.DEBUG)
    fh = logging.FileHandler(log_dir + default_log_name)
    logging.basicConfig(filemode=file_mode)
    fh.setLevel(file_log_level)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    fh.setFormatter(formatter)
    myLOGGER.addHandler(fh)

def myLOGGER_build():
    time_str = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    data_preprocess_log_file = 'pr_model_rfr_sample_' + str(time_str[5:7]) + str(time_str[8:10]) \
                               + "_" + time_str[11:13] + str(time_str[14:16]) + ".txt"
    logger_custom(data_preprocess_log_file)

myLOGGER_build()

# Load dataset.
def load_data(dir_train, dir_test):
    myLOGGER.debug("Load train and test data begin...")
    train_df = pd.read_csv(dir_train)
    test_df = pd.read_csv(dir_test)
    myLOGGER.debug("Train shape {0}, test shape {1}.".format(train_df.shape, test_df.shape))

    train_label = train_df['label']
    test_label = test_df['label']
    train_df.drop(labels='label', inplace=True, axis=1)
    test_df.drop(labels='label', inplace=True, axis=1)

    return train_df, train_label, test_df, test_label

def load_data_for_cross_validation(dir_train, dir_test):
    myLOGGER.debug("Load train and test data for cross validation begin...")
    train_df = pd.read_csv(dir_train)
    test_df = pd.read_csv(dir_test)
    myLOGGER.debug("Train shape {0}, test shape {1}.".format(train_df.shape, test_df.shape))

   # train_label = train_df['label']
    test_label = test_df['label']
   # train_df.drop(labels='label', inplace=True, axis=1)
    test_df.drop(labels='label', inplace=True, axis=1)

    '''train_len = len(train_df)
    test_len = len(test_df)
    total_df = pd.concat([train_df,test_df],axis = 0)

    total_df  = total_df.sample(n = 1000,replace=True,axis=1)
    

    train_df = total_df.iloc[0:train_len,:]
    test_len = total_df.iloc[train_len:len(total_df),:]

    train_df = pd.concat([train_df,train_label],axis = 1)'''

    return train_df, test_df,test_label    
    

# Cross validation
def cross_validation_data_preprocess():
    myLOGGER.debug('Cross validation on train data....')
    '''rf = ensemble.RandomForestClassifier(n_estimators=100, min_samples_split=10, min_samples_leaf=10,
                                         max_features='auto', max_depth=15, oob_score=True, random_state=10)
    scores = cross_val_score(rf, train_df, train_label, cv=2)'''
    train_df,test_df,test_label = load_data_for_cross_validation(train_dir, test_dir)
    train_set = []
    train_label_set = []
    for i in range(4):
        train_df, train = model_selection.train_test_split(train_df, test_size=1/2,random_state=10)  
        train_label = train['label']
        train.drop(labels='label', inplace=True, axis=1)
        train_label_set.append(train_label)
        train_set.append(train)
    train_df_label = train_df['label']
    train_df.drop(labels='label', inplace=True, axis=1)
    train_label_set.append(train_df_label)
    train_set.append(train_df)
    myLOGGER.debug("Cross validation end!")
    
    return train_set,train_label_set,test_df,test_label
    
def cross_validation_data_preprocess_2():
    myLOGGER.debug('Cross validation on train data....')
    '''rf = ensemble.RandomForestClassifier(n_estimators=100, min_samples_split=10, min_samples_leaf=10,
                                         max_features='auto', max_depth=15, oob_score=True, random_state=10)
    scores = cross_val_score(rf, train_df, train_label, cv=2)'''
    train_df,test_df,test_label = load_data_for_cross_validation(train_dir, test_dir)
    train_set = []
    train_label_set = []
    for i in range(1):
        train_df, train = model_selection.train_test_split(train_df, test_size=1/2,random_state=10)  
        train_label = train['label']
        train.drop(labels='label', inplace=True, axis=1)
        train_label_set.append(train_label)
        train_set.append(train)
    train_df_label = train_df['label']
    train_df.drop(labels='label', inplace=True, axis=1)
    train_label_set.append(train_df_label)
    train_set.append(train_df)
    myLOGGER.debug("Cross validation end!")
    
    return train_set,train_label_set,test_df,test_label
    
    
def model_cross_validation(flag):
    if flag == 5:
        train_set,train_label_set,test_df,test_label = cross_validation_data_preprocess()
    else:
        train_set,train_label_set,test_df,test_label = cross_validation_data_preprocess_2()
    myLOGGER.debug('Cross validation varify on train data....')
    bf1_RF = -1.0
    index_RF = 0
    bf1_SVM = -1.0
    index_SVM = 0
    bf1_DT = -1.0
    index_DT = 0
    bf1_KNN = -1.0
    index_KNN = 0
    for i in range(len(train_set)):
        train_merge = DataFrame()
        train_label_merge = Series()
        for j in range(len(train_set)):
            if i != j :      
                train_label_merge = train_label_merge.append(train_label_set[j])
                train_merge = train_merge.append(train_set[j])
        myLOGGER.debug("Train shape {0}, train label {1},test shape {2}, test label shape {3}.".format(train_merge.shape, len(train_label_merge), train_set[i].shape,len(train_label_set[i])))
        
        f1_RF = model_RandomForest(train_merge, train_label_merge, train_set[i], train_label_set[i])
        if f1_RF > bf1_RF:
            bf1_RF = f1_RF
            index_RF = i


        f1_RF = model_SVM(train_merge, train_label_merge, train_set[i], train_label_set[i])
        if f1_RF > bf1_SVM:
            bf1_SVM = f1_RF
            index_SVM = i


        f1_DT = model_DT(train_merge, train_label_merge, train_set[i], train_label_set[i])
        if f1_DT > bf1_DT:
            bf1_DT = f1_DT
            index_DT = i
        f1_KNN = model_KNN(train_merge, train_label_merge, train_set[i], train_label_set[i])
        if f1_KNN > bf1_KNN:
            bf1_KNN = f1_KNN
            index_KNN = i
    myLOGGER.debug("Cross validation varify: f1_RF = {0}, f1_SVM = {1}, f1_DT = {2}, f1_KNN = {3}.".format(bf1_RF, bf1_SVM, bf1_DT, bf1_KNN))
    
    
    myLOGGER.debug('Cross validation varify on test data....')
    
    train_merge = DataFrame()
    train_label_merge = Series()
    for j in range(len(train_set)):
        if j != index_RF :
            train_label_merge = train_label_merge.append(train_label_set[j])
            train_merge = train_merge.append(train_set[j])
    f1_RF = model_RandomForest(train_merge, train_label_merge, test_df, test_label)
    
    train_merge = DataFrame()
    train_label_merge = Series()
    for j in range(len(train_set)):
        if j != index_SVM :
            train_label_merge = train_label_merge.append(train_label_set[j])
            train_merge = train_merge.append(train_set[j])
    f1_SVM = model_SVM(train_merge, train_label_merge, test_df, test_label)
    
    


    train_merge = DataFrame()
    train_label_merge = Series()
    for j in range(len(train_set)):
        if j != index_DT :
            train_label_merge = train_label_merge.append(train_label_set[j])
            train_merge = train_merge.append(train_set[j])
    f1_DT = model_DT(train_merge, train_label_merge, test_df, test_label)
    


    train_merge = DataFrame()
    train_label_merge = Series()
    for j in range(len(train_set)):
        if j != index_KNN :
            train_label_merge = train_label_merge.append(train_label_set[j])
            train_merge = train_merge.append(train_set[j])
    f1_KNN = model_KNN(train_merge, train_label_merge, test_df, test_label)



# Feature select by RandomForest feature importance.
def feature_select_by_importance(train_df, train_label, test_df, test_label):
    myLOGGER.debug(
        'Training data shape={0},  test data shape={1}.'.format(train_df.shape, test_df.shape))
    myLOGGER.debug('Fit train data....')
    
    rf = ensemble.RandomForestClassifier(n_estimators=100, min_samples_split=10, min_samples_leaf=10,
                                         max_features='auto', max_depth=15, oob_score=True, random_state=10)
    rf.fit(train_df, train_label)
    names = np.array(train_df.columns)
    sorted_features_map = sorted(zip(map(lambda x: round(x, 4), rf.feature_importances_), names), reverse=True)
    myLOGGER.debug("Features sorted by their score: {0}.".format(sorted_features_map))

    feature_importance_threshold = 0.0012
    selected_features = [val for key, val in sorted_features_map if key >= feature_importance_threshold ]
    myLOGGER.debug("After feature selected with feature_importance_threshold = {0}, feature number is {1}."
                 .format(feature_importance_threshold ,len(selected_features)))
    myLOGGER.debug("Begin save feature selected data...")
    train_df['label'] = train_label
    test_df['label'] = test_label
    selected_features.append('label')
    train_df[selected_features].to_csv(parent_path + "ped_train_04.csv", index=None)
    test_df[selected_features].to_csv(parent_path + "ped_test_04.csv", index=None)
    myLOGGER.debug("Save feature selected data end...")

# Model train and test.
def model_RandomForest(train_df, train_label, test_df, test_label):
    # train_df, train_label, test_df, test_label = load_data()
    myLOGGER.debug('[RF]Predict test data....')
    estimator_set = [2,5,10,20,50,60,70,100,120,140,150,180,200]
    samples_split_set = [2,3,4,5,6,7,8,9,10,11,12,13,14,15]
    samples_leaf_set = [2,3,4,5,6,7,8,9,10,11,12,13,14,15]
    bf1_RF = -1.0
    best_RF1 = 0
    myLOGGER.debug("[RF] Begin estimator number")
    for estimator_num in estimator_set:
        rf = ensemble.RandomForestClassifier(n_estimators=estimator_num, min_samples_split=2, min_samples_leaf=2,
                                             max_features='auto', max_depth=None, oob_score=True, random_state=10)
        rf.fit(train_df, train_label.ravel())

        results = rf.predict(test_df)
        # precision, recall, _ = precision_recall_curve(test_label.ravel(), results)
        precision = metrics.precision_score(test_label.ravel(), results)
        recall = metrics.recall_score(test_label.ravel(), results)
        f1 = metrics.f1_score(test_label.ravel(), results)
        if f1 > bf1_RF:
            bf1_RF = f1
            best_RF1 = estimator_num
        myLOGGER.debug("[RF]Predict result: precision = {0}, recall = {1}, f1 = {2}, estimator number = {3}.".format(precision, recall, f1, estimator_num))
    myLOGGER.debug("[RF] Begin min samples split number")
    bf1_RF = -1.0
    best_RF2 = 0
    for samples_split in samples_split_set:
        rf = ensemble.RandomForestClassifier(n_estimators=best_RF1, min_samples_split=samples_split, min_samples_leaf=2,
                                             max_features='auto', max_depth=None, oob_score=True, random_state=10)
        rf.fit(train_df, train_label.ravel())

        results = rf.predict(test_df)
        # precision, recall, _ = precision_recall_curve(test_label.ravel(), results)
        precision = metrics.precision_score(test_label.ravel(), results)
        recall = metrics.recall_score(test_label.ravel(), results)
        f1 = metrics.f1_score(test_label.ravel(), results)
        if f1 > bf1_RF:
            bf1_RF = f1
            best_RF2 = samples_split
        myLOGGER.debug("[RF]Predict result: precision = {0}, recall = {1}, f1 = {2}, samples split = {3}.".format(precision, recall, f1, samples_split))
    myLOGGER.debug("[RF] Begin min samples leaf number")
    bf1_RF = -1.0
    best_RF3 = 0
    for samples_leaf in samples_leaf_set:
        rf = ensemble.RandomForestClassifier(n_estimators=best_RF1, min_samples_split=best_RF2, min_samples_leaf=samples_leaf,
                                             max_features='auto', max_depth=None, oob_score=True, random_state=10)
        rf.fit(train_df, train_label.ravel())

        results = rf.predict(test_df)
        # precision, recall, _ = precision_recall_curve(test_label.ravel(), results)
        precision = metrics.precision_score(test_label.ravel(), results)
        recall = metrics.recall_score(test_label.ravel(), results)
        f1 = metrics.f1_score(test_label.ravel(), results)
        if f1 > bf1_RF:
            bf1_RF = f1
            best_RF3 = samples_leaf
        myLOGGER.debug("[RF]Predict result: precision = {0}, recall = {1}, f1 = {2}, samples leaf = {3}.".format(precision, recall, f1, samples_leaf))


    rf = ensemble.RandomForestClassifier(n_estimators=best_RF1, min_samples_split=best_RF2, min_samples_leaf=best_RF3,
                                             max_features='auto', max_depth=None, oob_score=True, random_state=10)
    rf.fit(train_df, train_label)

    results = rf.predict(test_df)
        # precision, recall, _ = precision_recall_curve(test_label.ravel(), results)
    precision = metrics.precision_score(test_label.ravel(), results)
    recall = metrics.recall_score(test_label.ravel(), results)
    f1 = metrics.f1_score(test_label.ravel(), results)
       
    myLOGGER.debug("[RF]Predict best result: precision = {0}, recall = {1}, f1 = {2}, estimator number = {3}, samples_split = {4}, samples leaf = {5}.".format(precision, recall, f1, best_RF1,best_RF2 ,best_RF3))
    
    myLOGGER.debug('[RF]Predict end...')
    return f1

#Model train and test by svm.
def model_SVM(train_df, train_label, test_df, test_label):
    myLOGGER.debug('[svm] Predict test data....')
    clf = svm.SVC()
    clf.fit(train_df, train_label.ravel())
    results = clf.predict(test_df)
    results[1] = 1
    precision = metrics.precision_score(test_label.ravel(), results)
    recall = metrics.recall_score(test_label.ravel(), results)
    f1 = metrics.f1_score(test_label.ravel(), results)
    myLOGGER.debug("[svm] Predict result: precision = {0}, recall = {1}, f1 = {2}.".format(precision, recall, f1))
    myLOGGER.debug('[svm] Predict end...')
    return f1

def model_DT(train_df, train_label, test_df, test_label):
    myLOGGER.debug('[DT] Predict test data....')
    clf = tree.DecisionTreeClassifier()
    clf.fit(train_df, train_label.ravel())
    results = clf.predict(test_df)
    precision = metrics.precision_score(test_label.ravel(), results)
    recall = metrics.recall_score(test_label.ravel(), results)
    f1 = metrics.f1_score(test_label.ravel(), results)
    myLOGGER.debug("[DT] Predict result: precision = {0}, recall = {1}, f1 = {2}.".format(precision, recall, f1))
    myLOGGER.debug('[DT] Predict end...')
    return f1
    
def model_KNN(train_df, train_label, test_df, test_label):
    myLOGGER.debug('[KNN] Predict test data....')
    neighbor_set = [2,5,10,15,20]
    bf1_KNN = -1.0
    best_KNN = 0
    for neighbor_num in neighbor_set:
        clf = KNeighborsClassifier(n_neighbors=neighbor_num, algorithm='kd_tree').fit(train_df,train_label.ravel())  
        results = clf.predict(test_df)
        precision = metrics.precision_score(test_label.ravel(), results)
        recall = metrics.recall_score(test_label.ravel(), results)
        f1 = metrics.f1_score(test_label.ravel(), results)
        if f1 > bf1_KNN:
            bf1_KNN = f1
            best_KNN = neighbor_num
        myLOGGER.debug("[KNN] Predict result: precision = {0}, recall = {1}, f1 = {2}, neighbor_number = {3}.".format(precision, recall, f1, neighbor_num))
    
    clf = KNeighborsClassifier(n_neighbors=best_KNN, algorithm="ball_tree").fit(train_df,train_label)  

    results = clf.predict(test_df)
    precision = metrics.precision_score(test_label.ravel(), results)
    recall = metrics.recall_score(test_label.ravel(), results)
    f1 = metrics.f1_score(test_label.ravel(), results)
    
    myLOGGER.debug("[KNN] Predict best result: precision = {0}, recall = {1}, f1 = {2}, neighbor_number = {3}.".format(precision, recall, f1, neighbor_num))
    myLOGGER.debug('[KNN] Predict end...')    
    return f1


def model_call():
    myLOGGER.debug('Direct Experiments')   
    train_df, train_label, test_df, test_label = load_data(train_dir, test_dir)

    model_SVM(train_df, train_label, test_df, test_label)
    model_RandomForest(train_df, train_label, test_df, test_label)
    
    model_DT(train_df, train_label, test_df, test_label)
    model_KNN(train_df, train_label, test_df, test_label)

    myLOGGER.debug('...')
    myLOGGER.debug('...')   
    myLOGGER.debug('...')   
    myLOGGER.debug('...')   
    myLOGGER.debug('...')
    myLOGGER.debug('Cross validation Experiments_2')

    model_cross_validation(2)


    myLOGGER.debug('...')
    myLOGGER.debug('...')   
    myLOGGER.debug('...')   
    myLOGGER.debug('...')   
    myLOGGER.debug('...')
    myLOGGER.debug('Cross validation Experiments_5')

    model_cross_validation(5)
    
    myLOGGER.debug('END Experiments')   


def model_call_ori():
    train_df, train_label, test_df, test_label = load_data(train_dir, test_dir)
    myLOGGER.debug(
        'Training data shape={0},  test data shape={1}.'.format(train_df.shape, test_df.shape))
    myLOGGER.debug('Fit train data....')
    rf = ensemble.RandomForestClassifier(n_estimators=100, min_samples_split=10, min_samples_leaf=10,
                                         max_features='auto', max_depth=15, oob_score=True, random_state=10)
    rf.fit(train_df, train_label)
   # model_cross_validation(train_df, train_label)

    # model_RandomForest(train_df, train_label, test_df, test_label)

if __name__ == '__main__':
    # train_df, train_label, test_df, test_label = load_data(train_dir, test_dir)
    # feature_select_by_importance(train_df, train_label, test_df, test_label)

    # train_df, train_label, test_df, test_label = load_data(train_dir, test_dir)
    # feature_dimension_decrease(train_df, train_label, test_df, test_label)

    model_call()
    # model_call_ori()
