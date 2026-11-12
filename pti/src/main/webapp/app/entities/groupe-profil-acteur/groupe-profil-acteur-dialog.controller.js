(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupeProfilActeurDialogController', GroupeProfilActeurDialogController);

    GroupeProfilActeurDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'GroupeProfilActeur','RequestUtils','isViewing','isEdit'];

    function GroupeProfilActeurDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, GroupeProfilActeur,RequestUtils,isViewing,isEdit) {
        var vm = this;

        vm.groupeProfilActeur = entity;
        vm.clear = clear;
        vm.save = save;
        vm.isViewing = isViewing;
        vm.isEdit = isEdit;
        vm.fournisseurservices=[];
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
        RequestUtils.GetFournisseur.query({}, function (datas) {
            vm.fournisseurservices = datas;
        });

        function save () {
            vm.isSaving = true;
            if (vm.groupeProfilActeur.id !== null) {
                GroupeProfilActeur.update(vm.groupeProfilActeur, onSaveSuccess, onSaveError);
            } else {
                GroupeProfilActeur.save(vm.groupeProfilActeur, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:groupeProfilActeurUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
