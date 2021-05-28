var User = function(){
    this.init = function(){
        //模拟上传excel
        $("#uploadEventBtn").unbind("click").bind("click",function(){
            $("#uploadEventFile").click();
        });
        $("#uploadEventFile").bind("change",function(){
            $("#uploadEventPath").attr("value",$("#uploadEventFile").val());
        });
    };
    //点击上传按钮
    this.uploadBtn = function(){
        var uploadEventFile = $("#uploadEventFile").val();
        if(uploadEventFile == ''){
            alert("请选择excel,再上传");
        }else if(uploadEventFile.lastIndexOf(".xls")<0){//可判断以.xls和.xlsx结尾的excel
            alert("只能上传Excel文件");
        }else{
            var url =  '/upload/';
            var formData = new FormData($('form')[0]);
            user.sendAjaxRequest(url,'POST',formData);
        }
    };
    this.sendAjaxRequest = function(url,type,data){
        $.ajax({
            url : url,
            type : type,
            data : data,
            success : function(result) {
                //alert( result);
            },
            error : function() {
                alert( "excel上传失败");
            },
            cache : false,
            contentType : false,
            processData : false
        });
    };
}
var user;
$(function(){
    user = new User();
    user.init();
});

function uploadExcel() {
    var formData = new FormData();
    formData.append('excelFile', $('#excelFile')[0].files[0]);
    $.ajax({
        type: "post",
        processData: false,
        contentType: false,
        url: "/importExcel",
        data: formData,
        success: function (data) {

        }
    });

}