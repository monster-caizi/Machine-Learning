# 要点：

* 数据解析的难点在于Table的间的连接；
* 如何将数据尽可能减少冗余的情况下解析每一天所代表的Excel中多线路正反方向下三角矩阵数据的解析与划分，并导入oracle数据库中（约1400行Java代码）；
* 通过PL/SQL程序块解析提取数据库中用于数据分析的有效数据（约8段PL/SQL程序）。