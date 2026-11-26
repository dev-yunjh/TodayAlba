-- 근무지
CREATE TABLE workplaces (
                            id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            user_id     BINARY(16)   NOT NULL,
                            name        VARCHAR(100) NOT NULL,
                            color       CHAR(7)      NOT NULL DEFAULT '#A78BFA',
                            active      BOOLEAN      NOT NULL DEFAULT TRUE,
                            sort_order  INT          NOT NULL DEFAULT 0,
                            created_at  DATETIME     NOT NULL,
                            updated_at  DATETIME     NOT NULL,

    -- 회원 참조
                            CONSTRAINT fk_workplaces_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id),

    -- 근무지명 공백 방지
                            CONSTRAINT chk_workplaces_name
                                CHECK (CHAR_LENGTH(TRIM(name)) > 0),

    -- HEX 색상 형식 검증
                            CONSTRAINT chk_workplaces_color
                                CHECK (color REGEXP '^#[0-9A-Fa-f]{6}$'),

    -- 재직 상태 검증
    CONSTRAINT chk_workplaces_active
        CHECK (active IN (0, 1)),

    -- 표시 순서 음수 방지
    CONSTRAINT chk_workplaces_sort_order
        CHECK (sort_order >= 0),

    CONSTRAINT chk_workplaces_timestamp
         CHECK (updated_at >= created_at),

    -- 회원별 근무지 목록 조회 인덱스
    INDEX idx_workplaces_list
        (user_id, active, sort_order, id)


) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 근무지별 급여 설정
CREATE TABLE workplace_pay_settings (
                                        workplace_id                 BIGINT        NOT NULL PRIMARY KEY,
    -- 급여 산정 방식
                                        pay_type                     VARCHAR(10)   NOT NULL DEFAULT 'HOURLY',
                                        hourly_wage                  BIGINT        NULL,
                                        daily_wage                   BIGINT        NULL,
    -- 급여 지급 주기
                                        pay_cycle                    VARCHAR(10)   NOT NULL DEFAULT 'MONTHLY',
    -- 월급 지급 설정
                                        monthly_payday_mode          VARCHAR(10)   NULL,
                                        monthly_payday_day           TINYINT       NULL,
                                        monthly_payday_week          TINYINT       NULL,
                                        monthly_payday_weekday       TINYINT       NULL,
    -- 주급 및 격주 지급 설정
                                        weekly_payday_week_offset    TINYINT       NULL,
                                        weekly_payday_weekday        TINYINT       NULL,
                                        biweekly_period_anchor_date  DATE          NULL,
    -- 일급 지급 설정
                                        daily_pay_delay_days         TINYINT       NULL,
    -- 세금 및 공제 설정
                                        tax_type                     VARCHAR(20)   NOT NULL DEFAULT 'NONE',
                                        custom_tax_rate              DECIMAL(5,2)  NULL,
    -- 주휴수당 설정
                                        weekly_holiday_enabled       BOOLEAN       NOT NULL DEFAULT FALSE,
                                        weekly_holiday_start_weekday TINYINT       NULL,
    -- 공통 관리 필드
                                        created_at                   DATETIME      NOT NULL,
                                        updated_at                   DATETIME      NOT NULL,
    -- 외래키
                                        CONSTRAINT fk_pay_settings_workplace
                                            FOREIGN KEY (workplace_id)
                                                REFERENCES workplaces(id),
    -- 급여 산정 방식 검증
                                        CONSTRAINT chk_pay_settings_type
                                            CHECK (pay_type IN ('HOURLY', 'DAILY')),

                                        CONSTRAINT chk_pay_settings_hourly_wage
                                            CHECK (hourly_wage >= 0),

                                        CONSTRAINT chk_pay_settings_daily_wage
                                            CHECK (daily_wage >= 0),

                                        CONSTRAINT chk_pay_settings_wage
                                            CHECK (
                                                (
                                                    pay_type = 'HOURLY'
                                                    AND hourly_wage IS NOT NULL
                                                    AND daily_wage  IS NULL
                                                    )
                                                    OR
                                                (
                                                    pay_type = 'DAILY'
                                                    AND hourly_wage IS NULL
                                                    AND daily_wage  IS NOT NULL
                                                    )
                                                ),
    -- 급여 지급 주기 검증
                                        CONSTRAINT chk_pay_settings_cycle
                                            CHECK (pay_cycle IN (
                                                                 'MONTHLY', 'WEEKLY', 'BIWEEKLY', 'DAILY'
                                                )),

    -- 월급 설정 검증
                                        CONSTRAINT chk_pay_settings_monthly_day
                                            CHECK (monthly_payday_day BETWEEN 1 AND 31),

                                        CONSTRAINT chk_pay_settings_monthly_week
                                            CHECK (monthly_payday_week BETWEEN 1 AND 5),

                                        CONSTRAINT chk_pay_settings_monthly_weekday
                                            CHECK (monthly_payday_weekday BETWEEN 1 AND 7),

                                        CONSTRAINT chk_pay_settings_monthly_rule
                                            CHECK (
                                                (
                                                    pay_cycle = 'MONTHLY'
                                                        AND monthly_payday_mode IS NOT NULL
                                                        AND (
                                                        (
                                                            monthly_payday_mode = 'DATE'
                                                                AND monthly_payday_day IS NOT NULL
                                                                AND monthly_payday_week IS NULL
                                                                AND monthly_payday_weekday IS NULL
                                                            )
                                                            OR
                                                        (
                                                            monthly_payday_mode = 'WEEKDAY'
                                                                AND monthly_payday_day IS NULL
                                                                AND monthly_payday_week IS NOT NULL
                                                                AND monthly_payday_weekday IS NOT NULL
                                                            )
                                                        )
                                                    )
                                                    OR
                                                (
                                                    pay_cycle <> 'MONTHLY'
                                                        AND monthly_payday_mode IS NULL
                                                        AND monthly_payday_day IS NULL
                                                        AND monthly_payday_week IS NULL
                                                        AND monthly_payday_weekday IS NULL
                                                    )
                                                ),

    -- 주급 및 격주 설정 검증
                                        CONSTRAINT chk_pay_settings_weekly_offset
                                            CHECK (weekly_payday_week_offset IN (1, 2)),

                                        CONSTRAINT chk_pay_settings_weekly_weekday
                                            CHECK (weekly_payday_weekday BETWEEN 1 AND 7),

                                        CONSTRAINT chk_pay_settings_weekly_rule
                                            CHECK (
                                                (
                                                    pay_cycle IN ('WEEKLY', 'BIWEEKLY')
                                                        AND weekly_payday_week_offset IS NOT NULL
                                                        AND weekly_payday_weekday IS NOT NULL
                                                    )
                                                    OR
                                                (
                                                    pay_cycle NOT IN ('WEEKLY', 'BIWEEKLY')
                                                        AND weekly_payday_week_offset IS NULL
                                                        AND weekly_payday_weekday IS NULL
                                                    )
                                                ),

    -- 격주 정산 기준일 검증
                                        CONSTRAINT chk_pay_settings_biweekly_rule
                                            CHECK (
                                                (
                                                    pay_cycle = 'BIWEEKLY'
                                                        AND biweekly_period_anchor_date IS NOT NULL
                                                    )
                                                    OR
                                                (
                                                    pay_cycle <> 'BIWEEKLY'
                                                        AND biweekly_period_anchor_date IS NULL
                                                    )
                                                ),

    -- 일급 지급 설정 검증
                                        CONSTRAINT chk_pay_settings_daily_delay
                                            CHECK (daily_pay_delay_days IN (0, 1)),

                                        CONSTRAINT chk_pay_settings_daily_rule
                                            CHECK (
                                                (
                                                    pay_cycle = 'DAILY'
                                                        AND daily_pay_delay_days IS NOT NULL
                                                    )
                                                    OR
                                                (
                                                    pay_cycle <> 'DAILY'
                                                        AND daily_pay_delay_days IS NULL
                                                    )
                                                ),

    -- 세금 및 공제 설정 검증
                                        CONSTRAINT chk_pay_settings_tax_type
                                            CHECK (tax_type IN (
                                                                'NONE', 'WITHHOLDING_3_3', 'INSURANCE', 'CUSTOM'
                                                )),

                                        CONSTRAINT chk_pay_settings_custom_tax
                                            CHECK (
                                                (
                                                    tax_type = 'CUSTOM'
                                                        AND custom_tax_rate IS NOT NULL
                                                        AND custom_tax_rate BETWEEN 0 AND 100
                                                    )
                                                    OR
                                                (
                                                    tax_type <> 'CUSTOM'
                                                        AND custom_tax_rate IS NULL
                                                    )
                                                ),

    -- 주휴수당 사용 여부에 따른 시작 요일 검증
                                        CONSTRAINT chk_pay_settings_holiday_weekday
                                            CHECK (
                                                (
                                                    weekly_holiday_enabled = TRUE
                                                        AND weekly_holiday_start_weekday IS NOT NULL
                                                        AND weekly_holiday_start_weekday BETWEEN 1 AND 7
                                                    )
                                                    OR
                                                (
                                                    weekly_holiday_enabled = FALSE
                                                        AND weekly_holiday_start_weekday IS NULL
                                                    )
                                                ),

    -- 생성일 및 수정일 검증
                                        CONSTRAINT chk_pay_settings_timestamp
                                            CHECK (updated_at >= created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 일일 근무 기록
-- 일일 근무 기록 및 급여
CREATE TABLE work_shifts (
                             id                         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '근무 기록 ID',
                             workplace_id               BIGINT        NOT NULL COMMENT '개인 근무지 ID',

    -- 근무 날짜 및 시간
                             work_date                  DATE          NOT NULL COMMENT '근무 시작 날짜',
                             start_at                   DATETIME      NOT NULL COMMENT '근무 시작 일시',
                             end_at                     DATETIME      NOT NULL COMMENT '근무 종료 일시',
                             break_minutes              INT           NOT NULL DEFAULT 0 COMMENT '전체 휴게시간(분)',
                             night_break_minutes        INT           NOT NULL DEFAULT 0 COMMENT '야간 휴게시간(분)',

    -- 급여 산정 방식 스냅샷
                             pay_type                   VARCHAR(10)   NOT NULL COMMENT 'HOURLY: 시급제, DAILY: 일급제',
                             hourly_wage                BIGINT        NULL COMMENT '해당 근무에 적용된 시급',
                             daily_wage                 BIGINT        NULL COMMENT '해당 근무에 적용된 일급',

    -- 급여 지급 예정일 스냅샷
                             expected_payment_date     DATE           NOT NULL COMMENT '서버에서 자동 산정한 급여 지급 예정일',

    -- 급여 공제 설정 스냅샷
                             tax_type                   VARCHAR(20)   NOT NULL DEFAULT 'NONE' COMMENT '급여 공제 유형',
                             custom_tax_rate            DECIMAL(5,2)  NULL COMMENT '사용자 지정 공제율(%)',

    -- 야간근로수당
                             night_pay_rate             DECIMAL(5,2)  NULL COMMENT '야간수당 지급 비율(%), NULL은 미적용',
                             night_allowance_minutes    INT           NULL COMMENT '야간수당 적용 시간(분)',

    -- 연장근로수당
                             overtime_pay_rate          DECIMAL(5,2)  NULL COMMENT '연장수당 지급 비율(%), NULL은 미적용',
                             overtime_allowance_minutes INT           NULL COMMENT '연장수당 적용 시간(분)',

    -- 휴일근로수당
                             holiday_pay_rate           DECIMAL(5,2)  NULL COMMENT '휴일근로수당 지급 비율(%), NULL은 미적용',
                             holiday_allowance_minutes  INT           NULL COMMENT '휴일근로수당 적용 시간(분)',

    -- 기본급 지급 비율
                             pay_percentage             DECIMAL(5,2)  NULL COMMENT '기본급 지급 비율(%), NULL은 정상 지급',

    -- 근무 메모 및 급여 계산 결과
                             memo                       VARCHAR(1000) NULL COMMENT '근무일별 메모',
                             estimated_pay              BIGINT        NOT NULL COMMENT '서버에서 계산한 공제 전 예상 급여',

    -- 공통 관리 필드
                             created_at                 DATETIME      NOT NULL COMMENT '생성일시',
                             updated_at                 DATETIME      NOT NULL COMMENT '수정일시',

    -- 근무지 외래키
                             CONSTRAINT fk_work_shifts_workplace
                                 FOREIGN KEY (workplace_id)
                                     REFERENCES workplaces(id),

    -- 근무 날짜와 시작일 일치 여부
                             CONSTRAINT chk_work_shifts_work_date
                                 CHECK (work_date = DATE(start_at)),

    -- 급여 지급 예정일은 근무일 이전일 수 없음
                             CONSTRAINT chk_work_shifts_payment_date
                                 CHECK (expected_payment_date >= work_date),

    -- 근무 종료 일시는 시작 일시보다 이후여야 함
    CONSTRAINT chk_work_shifts_work_time
        CHECK (end_at > start_at),

    -- 전체 휴게시간 검증
    CONSTRAINT chk_work_shifts_break_minutes
        CHECK (
            break_minutes >= 0
            AND break_minutes <
                TIMESTAMPDIFF(MINUTE, start_at, end_at)
        ),

    -- 야간 휴게시간은 전체 휴게시간을 초과할 수 없음
    CONSTRAINT chk_work_shifts_night_break
        CHECK (
            night_break_minutes BETWEEN 0 AND break_minutes
        ),

    -- 급여 산정 방식 검증
    CONSTRAINT chk_work_shifts_pay_type
        CHECK (pay_type IN ('HOURLY', 'DAILY')),

    -- 시급제와 일급제 상호 배타적 저장
    CONSTRAINT chk_work_shifts_wage
        CHECK (
            (
                pay_type = 'HOURLY'
                AND hourly_wage IS NOT NULL
                AND hourly_wage >= 0
                AND daily_wage IS NULL
            )
            OR
            (
                pay_type = 'DAILY'
                AND hourly_wage IS NULL
                AND daily_wage IS NOT NULL
                AND daily_wage >= 0
            )
        ),

    -- 급여 공제 유형 검증
    CONSTRAINT chk_work_shifts_tax_type
        CHECK (
            tax_type IN (
                'NONE',
                'WITHHOLDING_3_3',
                'INSURANCE',
                'CUSTOM'
            )
        ),

    -- 사용자 지정 공제율 검증
    CONSTRAINT chk_work_shifts_custom_tax
        CHECK (
            (
                tax_type = 'CUSTOM'
                AND custom_tax_rate IS NOT NULL
                AND custom_tax_rate BETWEEN 0 AND 100
            )
            OR
            (
                tax_type <> 'CUSTOM'
                AND custom_tax_rate IS NULL
            )
        ),

    -- 야간수당 검증
    CONSTRAINT chk_work_shifts_night_allowance
        CHECK (
            (
                night_pay_rate IS NULL
                AND night_allowance_minutes IS NULL
            )
            OR
            (
                night_pay_rate IS NOT NULL
                AND night_allowance_minutes IS NOT NULL
                AND night_pay_rate > 0
                AND night_allowance_minutes > 0
                AND night_allowance_minutes <=
                    TIMESTAMPDIFF(MINUTE, start_at, end_at)
                    - break_minutes
            )
        ),

    -- 연장수당 검증
    CONSTRAINT chk_work_shifts_overtime_allowance
        CHECK (
            (
                overtime_pay_rate IS NULL
                AND overtime_allowance_minutes IS NULL
            )
            OR
            (
                overtime_pay_rate IS NOT NULL
                AND overtime_allowance_minutes IS NOT NULL
                AND overtime_pay_rate > 0
                AND overtime_allowance_minutes > 0
                AND overtime_allowance_minutes <=
                    TIMESTAMPDIFF(MINUTE, start_at, end_at)
                    - break_minutes
            )
        ),

    -- 휴일근로수당 검증
    CONSTRAINT chk_work_shifts_holiday_allowance
        CHECK (
            (
                holiday_pay_rate IS NULL
                AND holiday_allowance_minutes IS NULL
            )
            OR
            (
                holiday_pay_rate IS NOT NULL
                AND holiday_allowance_minutes IS NOT NULL
                AND holiday_pay_rate > 0
                AND holiday_allowance_minutes > 0
                AND holiday_allowance_minutes <=
                    TIMESTAMPDIFF(MINUTE, start_at, end_at)
                    - break_minutes
            )
        ),

    -- 기본급 지급 비율 검증
    CONSTRAINT chk_work_shifts_pay_percentage
        CHECK (
            pay_percentage IS NULL
            OR pay_percentage BETWEEN 0 AND 100
        ),

    -- 예상 급여 음수 방지
    CONSTRAINT chk_work_shifts_estimated_pay
        CHECK (estimated_pay >= 0),

    CONSTRAINT chk_work_shifts_timestamp
        CHECK (updated_at >= created_at),

    -- 근무지별 캘린더 및 근무 기록 조회 인덱스
    INDEX idx_work_shifts_calendar
        (workplace_id, work_date, start_at, id),

    -- 근무지별 급여 지급 예정일 조회 인덱스
    INDEX idx_work_shifts_payment
        (workplace_id, expected_payment_date)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='일일 근무 기록 및 급여 관리';


-- 주휴수당 산정 및 확정 기록
CREATE TABLE weekly_holiday_pay (
                                    id                    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '주휴수당 기록 ID',
                                    workplace_id          BIGINT       NOT NULL COMMENT '개인 근무지 ID',

    -- 주휴수당 산정 기간
                                    week_start_date       DATE         NOT NULL COMMENT '주휴수당 산정 시작일',

    -- 주휴수당 산정 조건
                                    scheduled_minutes     INT          NULL COMMENT '소정근로시간(분)',
                                    attendance_status     VARCHAR(10)  NOT NULL DEFAULT 'UNKNOWN' COMMENT '출근 조건 충족 여부',
                                    holiday_paid_minutes  INT          NULL COMMENT '주휴 유급시간(분)',
                                    hourly_wage           BIGINT       NULL COMMENT '주휴수당 산정 시급',

    -- 주휴수당 확정 상태
                                    status                VARCHAR(10)  NOT NULL DEFAULT 'DRAFT' COMMENT '초안, 확정 상태',
                                    calculated_amount     BIGINT       NULL COMMENT '계산된 공제 전 주휴수당 금액',

    -- 급여 귀속 및 지급 예정일
                                    attribution_date      DATE         NULL COMMENT '급여 집계 귀속일',
                                    expected_payment_date DATE         NULL COMMENT '급여 지급 예정일',

    -- 급여 합산 설정
                                    include_in_payroll    BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '급여 합산 여부',

    -- 계산 버전 및 확정일
                                    calculation_version   INT          NOT NULL DEFAULT 1 COMMENT '주휴수당 계산 로직 버전',
                                    confirmed_at          DATETIME     NULL COMMENT '주휴수당 확정일시',

    -- 급여 공제 설정 스냅샷
                                    tax_type              VARCHAR(20)  NOT NULL DEFAULT 'NONE' COMMENT '주휴수당 산정 당시 급여 공제 유형',

                                    custom_tax_rate       DECIMAL(5,2) NULL COMMENT '주휴수당 산정 당시 사용자 지정 공제율(%)',

    -- 공통 관리 필드
                                    created_at            DATETIME     NOT NULL COMMENT '생성일시',
                                    updated_at            DATETIME     NOT NULL COMMENT '수정일시',

    -- 근무지 참조
                                    CONSTRAINT fk_weekly_holiday_pay_workplace
                                        FOREIGN KEY (workplace_id)
                                            REFERENCES workplaces(id),

    -- 동일 근무지 및 동일 주차 중복 등록 방지
                                    CONSTRAINT uq_weekly_holiday_pay_week
                                        UNIQUE (workplace_id, week_start_date),

    -- 소정근로시간 음수 방지
                                    CONSTRAINT chk_weekly_holiday_scheduled_minutes
                                        CHECK (scheduled_minutes >= 0),

    -- 출근 조건 충족 상태 검증
                                    CONSTRAINT chk_weekly_holiday_attendance
                                        CHECK (
                                            attendance_status IN ('UNKNOWN', 'YES', 'NO')
                                            ),

    -- 주휴 유급시간 음수 방지
                                    CONSTRAINT chk_weekly_holiday_paid_minutes
                                        CHECK (holiday_paid_minutes >= 0),

    -- 주휴수당 산정 시급 음수 방지
                                    CONSTRAINT chk_weekly_holiday_hourly_wage
                                        CHECK (hourly_wage >= 0),

    -- 주휴수당 상태 검증
                                    CONSTRAINT chk_weekly_holiday_status
                                        CHECK (status IN ('DRAFT', 'CONFIRMED')),

    -- 주휴수당 금액 음수 방지
                                    CONSTRAINT chk_weekly_holiday_amount
                                        CHECK (calculated_amount >= 0),

    -- 초안 및 확정 상태별 필수값 검증
                                    CONSTRAINT chk_weekly_holiday_confirmed
                                        CHECK (
                                            (
                                                status = 'DRAFT'
                                                    AND calculated_amount IS NULL
                                                    AND confirmed_at IS NULL
                                                )
                                                OR
                                            (
                                                status = 'CONFIRMED'
                                                    AND scheduled_minutes IS NOT NULL
                                                    AND attendance_status IN ('YES', 'NO')
                                                    AND holiday_paid_minutes IS NOT NULL
                                                    AND hourly_wage IS NOT NULL
                                                    AND calculated_amount IS NOT NULL
                                                    AND attribution_date IS NOT NULL
                                                    AND expected_payment_date IS NOT NULL
                                                    AND confirmed_at IS NOT NULL
                                                )
                                            ),

    -- 출근 조건 미충족 시 주휴수당 0원
                                    CONSTRAINT chk_weekly_holiday_ineligible
                                        CHECK (
                                            status <> 'CONFIRMED'
                                                OR attendance_status <> 'NO'
                                                OR (
                                                calculated_amount = 0
                                                    AND holiday_paid_minutes = 0
                                                )
                                            ),
                                    CONSTRAINT chk_weekly_holiday_tax_type
                                        CHECK (tax_type IN (
                                                            'NONE', 'WITHHOLDING_3_3', 'INSURANCE', 'CUSTOM'
                                            )),

                                    CONSTRAINT chk_weekly_holiday_custom_tax
                                        CHECK (
                                            (
                                                tax_type = 'CUSTOM'
                                                    AND custom_tax_rate IS NOT NULL
                                                    AND custom_tax_rate BETWEEN 0 AND 100
                                                )
                                                OR
                                            (
                                                tax_type <> 'CUSTOM'
                                                    AND custom_tax_rate IS NULL
                                                )
                                            ),

    -- 급여 귀속일은 주휴수당 산정 시작일 이후
                                    CONSTRAINT chk_weekly_holiday_attribution
                                        CHECK (attribution_date >= week_start_date),

    -- 급여 지급 예정일 검증
                                    CONSTRAINT chk_weekly_holiday_payment_date
                                        CHECK (expected_payment_date >= attribution_date),

    -- 급여 합산 여부 검증
                                    CONSTRAINT chk_weekly_holiday_include_payroll
                                        CHECK (include_in_payroll IN (0, 1)),

    -- 계산 로직 버전 검증
                                    CONSTRAINT chk_weekly_holiday_version
                                        CHECK (calculation_version >= 1),

    -- 주휴수당 확정일시 검증
                                    CONSTRAINT chk_weekly_holiday_confirmed_at
                                        CHECK (confirmed_at >= created_at),

    -- 생성일 및 수정일 검증
                                    CONSTRAINT chk_weekly_holiday_timestamp
                                        CHECK (updated_at >= created_at),

    -- 근무지별 급여 지급 예정일 조회
                                    INDEX idx_weekly_holiday_payment
                                        (workplace_id, expected_payment_date),

    -- 근무지별 월간 주휴수당 집계
                                    INDEX idx_weekly_holiday_attribution
                                        (workplace_id, attribution_date)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='근무지별 주휴수당 산정 및 확정 기록';