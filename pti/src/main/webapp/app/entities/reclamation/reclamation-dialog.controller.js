(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReclamationDialogController', ReclamationDialogController);
        ReclamationDialogController.$inject = ['$timeout','isViewing', '$scope', '$stateParams', '$uibModalInstance','$http', 'entity','Reclamation','TypeReclamation'];

    function ReclamationDialogController ($timeout,isViewing, $scope, $stateParams, $uibModalInstance,$http, entity,Reclamation,TypeReclamation) {
        var vm = this;
        vm.reclamation = entity;
        vm.isViewing = isViewing;
        vm.clear = clear;
        vm.save = save;

        //Initialisation des valeurs de selections
        vm.typeReclamations=[];
        vm.etats=['EN_INSTANCE', 'ATTRIBUE', 'RESOLU' , 'CLOS'];
        vm.qualiteAuteurs=[ 'AGENT', 'MARCHAND', 'CLIENT', 'ENTREPRISE'];
        vm.typeReclamations=TypeReclamation.query();
        //fin de l'initialisation

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.reclamation.id !== null) {
                Reclamation.update(vm.reclamation, onSaveSuccess, onSaveError);
            } else {
              //  var dateActuel=new Date();
              //  vm.reclamation.dateHeureCreation=new Date();
               // vm.reclamation.etat="EN_INSTANCE";
                Reclamation.save(vm.reclamation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:reclamationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }
    }
})();
