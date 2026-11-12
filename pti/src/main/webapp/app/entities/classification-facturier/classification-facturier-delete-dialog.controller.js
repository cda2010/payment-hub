(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClassificationFacturierDeleteController',ClassificationFacturierDeleteController);

    ClassificationFacturierDeleteController.$inject = ['$uibModalInstance', 'entity', 'ClassificationFacturier'];

    function ClassificationFacturierDeleteController($uibModalInstance, entity, ClassificationFacturier) {
        var vm = this;

        vm.classificationFacturier = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ClassificationFacturier.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
