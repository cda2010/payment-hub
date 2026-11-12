(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('RelationDeleteController',RelationDeleteController);

    RelationDeleteController.$inject = ['$uibModalInstance', 'entity', 'Relation'];

    function RelationDeleteController($uibModalInstance, entity, Relation) {
        var vm = this;

        vm.relation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Relation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
