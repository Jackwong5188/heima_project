package com.itheima.health.service;

import com.itheima.health.pojo.Member;

import java.util.List;

public interface MemberService {
    Member findMemberByTelephone(String telephone);

    void add(Member member);

    List<Integer> findMemberCountByMonth(List<String> months);
}
