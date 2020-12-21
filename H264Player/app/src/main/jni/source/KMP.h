//
// Created by Administrator on 2020/12/15.
//

#ifndef H264PLAYER_KMP_H
#define H264PLAYER_KMP_H
#include <iostream>
#include <string>
#include <vector>
namespace KMP{
    void getNextArr(int8_t *Sstr, int len,std::vector<int>& next);
    int KMPPos(int8_t* str1,int Llen, int8_t* str2,int Slen);
}

#endif //H264PLAYER_KMP_H
