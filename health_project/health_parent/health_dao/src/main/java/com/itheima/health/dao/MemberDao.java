package com.itheima.health.dao;

import com.itheima.health.pojo.Member;

public interface MemberDao {
    Member findMemberByTelephone(String telephone);

    void add(Member member);

    Integer findMemberCountBeforeDate(String m);

    Integer findMemberCountByDate(String today);

    Integer findMemberTotalCount();

    Integer findMemberCountAfterDate(String thisWeekMonday);
}
