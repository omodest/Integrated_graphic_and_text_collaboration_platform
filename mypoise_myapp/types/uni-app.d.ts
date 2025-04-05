/// <reference types="@dcloudio/types" />

declare namespace UniApp {
  interface RequestSuccessCallbackResult {
    data: {
      code: number;
      message: string;
      data: {
        records: any[];
        total: number;
        size: number;
        current: number;
      };
    };
    statusCode: number;
    header: any;
    cookies: string[];
  }
} 