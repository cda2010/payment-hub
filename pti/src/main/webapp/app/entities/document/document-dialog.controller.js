(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DocumentDialogController', DocumentDialogController);

    DocumentDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Document', 'TypeDocument'];

    function DocumentDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Document, TypeDocument) {
        var vm = this;

        vm.document = entity;
        vm.clear = clear;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.typedocuments = TypeDocument.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.document.id !== null) {
                Document.update(vm.document, onSaveSuccess, onSaveError);
            } else {
                Document.save(vm.document, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:documentUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


        vm.setValeur = function ($file, document) {
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        document.valeur = base64Data;
                        document.valeurContentType = $file.type;
                    });
                });
            }
        };

    }
})();
