(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InfoSuppDeleteController',InfoSuppDeleteController);

    InfoSuppDeleteController.$inject = ['$uibModalInstance', 'entity', 'InfoSupp'];

    function InfoSuppDeleteController($uibModalInstance, entity, InfoSupp) {
        var vm = this;

        vm.infosupp = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            InfoSupp.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
