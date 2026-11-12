(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreUtiliateurDeleteController',ParametreUtiliateurDeleteController);

    ParametreUtiliateurDeleteController.$inject = ['$uibModalInstance', 'entity', 'ParametreUtiliateur'];

    function ParametreUtiliateurDeleteController($uibModalInstance, entity, ParametreUtiliateur) {
        var vm = this;

        vm.parametreUtiliateur = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ParametreUtiliateur.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
