(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailHistoriqueDialogController', DetailHistoriqueDialogController);

    DetailHistoriqueDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'DetailHistorique', 'Historique'];

    function DetailHistoriqueDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, DetailHistorique, Historique) {
        var vm = this;

        vm.detailHistorique = entity;
        vm.clear = clear;
        vm.save = save;
        vm.historiques = Historique.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.detailHistorique.id !== null) {
                DetailHistorique.update(vm.detailHistorique, onSaveSuccess, onSaveError);
            } else {
                DetailHistorique.save(vm.detailHistorique, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:detailHistoriqueUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
