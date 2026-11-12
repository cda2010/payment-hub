(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TaxeOperationDetailController', TaxeOperationDetailController);

    TaxeOperationDetailController.$inject = ['$uibModalInstance','taxes','$scope','total'];

    function TaxeOperationDetailController($uibModalInstance,taxes,$scope,total) {
        var vm = this;
        vm.taxeOperations = taxes;
        vm.total=total;
        vm.clear=function clear () {
            $uibModalInstance.dismiss('cancel');
        }
    }
})();
