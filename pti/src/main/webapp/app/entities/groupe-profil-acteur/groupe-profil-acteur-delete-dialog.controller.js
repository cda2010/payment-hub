(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupeProfilActeurDeleteController',GroupeProfilActeurDeleteController);

    GroupeProfilActeurDeleteController.$inject = ['$uibModalInstance', 'entity', 'GroupeProfilActeur'];

    function GroupeProfilActeurDeleteController($uibModalInstance, entity, GroupeProfilActeur) {
        var vm = this;

        vm.groupeProfilActeur = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            GroupeProfilActeur.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
