(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CancelController', CancelController);

    CancelController.$inject = ['$uibModalInstance', 'msg','withForm'];

    function CancelController($uibModalInstance, msg,withForm) {
        var vm = this;
        vm.clear = clear;
        vm.confirmCancel = confirmCancel;
        vm.msg = msg;
        vm.withForm = withForm;
        vm.code=null;
        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmCancel(id) {
            console.log(vm.code);
            var datas={};
            datas.result=true;
            datas.code=vm.code;
            $uibModalInstance.close(datas);
        }
    }
})();