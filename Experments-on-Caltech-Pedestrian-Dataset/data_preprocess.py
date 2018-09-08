#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Train and test data set process.

</p>
@author: CaiZiHao
"""
import logging
import time
import pandas as pd
import numpy as np
import os



parent_path = "./PR_dataset/"
original_train = parent_path + "train/"
original_test = parent_path + "test/"
train_save = parent_path + "data_train.csv"
test_save = parent_path + "data_test.csv"
log_dir = "./log/"

# Field prefix.
field_prefix = "D"
# Data dimension.
feature_dimension = 2330

myLOGGER = logging.getLogger('myPreProcessLogger')

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
    data_preprocess_log_file = 'data_preprocess_' + str(time_str[5:7]) + str(time_str[8:10]) \
                               + "_" + time_str[11:13] + str(time_str[14:16]) + ".txt"
    logger_custom(data_preprocess_log_file)

myLOGGER_build()

# Function that obtain a list of files from a given folder.
def get_files(direc):
    full_files = []
    for root, dirs, files in os.walk(direc):
        for name in files:
            full_files.append((os.path.join(root, name), int(name.split(".")[0])))
    full_files.sort(key = lambda x : x[1])
    full_files = [file_tuple[0] for file_tuple in full_files ]
    return full_files


# Transform data format.
def data_transform(dirs):

    all_files1 = get_files(dirs[0])
    data_list1 = []
    for index, file_path in enumerate(all_files1):
        tmp_arr = np.loadtxt(file_path)
        tmp_arr = np.transpose(tmp_arr)
        data_list1.append(tmp_arr)
        myLOGGER.debug("Handle the {0}th file from {1}, shape: {2}.".format(index, file_path, tmp_arr.shape))
    tmp_df = pd.DataFrame(data=data_list1, columns=[(field_prefix + "_" + str(i)) for i in range(feature_dimension)], dtype=float)
    tmp_df['label'] = 1

    all_files2 = get_files(dirs[1])
    data_list2 = []
    for index, file_path in enumerate(all_files2):
        tmp_arr = np.loadtxt(file_path)
        tmp_arr = np.transpose(tmp_arr)
        data_list2.append(tmp_arr)
        myLOGGER.debug("Handle the {0}th file from {1}, shape: {2}.".format(index, file_path, tmp_arr.shape))
    total_df = pd.DataFrame(data=data_list2, columns=[(field_prefix + "_" + str(i)) for i in range(feature_dimension)], dtype=float)

    total_df['label'] = 0
    total_df = pd.concat([total_df, tmp_df], ignore_index=True, axis=0)
    return total_df


# Load data and transform its format which saving with suffix .csv.
def pre_process():
    myLOGGER.debug("Process train begin.")
    train_result = [original_train + 'pos/', original_train + 'neg/']
    train_df = data_transform(train_result)
    myLOGGER.debug("Save data begin.")
    train_df.to_csv(train_save, index=None)
	
    myLOGGER.debug("Process test begin.")
    test_result = [original_test + 'pos/', original_test + 'neg/']
    test_df = data_transform(test_result)
    myLOGGER.debug("Save data begin.")
    test_df.to_csv(test_save, index=None)
    myLOGGER.debug("End!")


if __name__ == '__main__':
	
	pre_process()
	pass
