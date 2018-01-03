package com.vm.dao.po;


import com.vm.base.utils.ByteConstantVar;

/**
 * Created by ZhangKe on 2017/12/11.
 */
public class BasePo {

    /**
     * 状态
     */
    public enum Status {
        NORMAL(ByteConstantVar.ONE, "正常"),
        FROZEN(ByteConstantVar.TWO, "冻结"),
        DELETED(ByteConstantVar.THREE, "删除");

        Byte code;

        String msg;

        Status(Byte code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Byte getCode() {
            return code;
        }

        public void setCode(Byte code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        /**
         * 是否删除状态
         *
         * @return
         */
        public static boolean isDeleted(Byte status) {
            if (status.equals(Status.DELETED.getCode())) {
                return true;
            }
            return false;
        }

        /**
         * 是否冻结状态
         *
         * @return
         */
        public static boolean isFrozen(Byte status) {
            if (status.equals(Status.FROZEN.getCode())) {
                return true;
            }
            return false;
        }

        /**
         * 是否正常状态
         *
         * @return
         */
        public static boolean isNormal(Byte status) {
            if (status.equals(Status.NORMAL.getCode())) {
                return true;
            }
            return false;
        }
    }

}
