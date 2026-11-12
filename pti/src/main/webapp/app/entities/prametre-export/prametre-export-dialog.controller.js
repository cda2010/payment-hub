(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PrametreExportDialogController', PrametreExportDialogController);

    PrametreExportDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'PrametreExport'];

    function PrametreExportDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, PrametreExport) {
        var vm = this;

        vm.prametreExport = entity;
        vm.clear = clear;
        vm.save = save;
        vm.typecode = ["BOOLEAN","DATE","NUMBER","TEXT"];
        vm.typeListe = ["LISTE_DYNAMIQUE","LISTE_STATIQUE"]
       
        vm.col = [];
        vm.IsList=false;
        if(vm.prametreExport.typeListe!=null){
            vm.IsList=true;
        }
        
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
        

        function save () {
            console.log(vm.prametreExport.id);
            vm.clearIfNotList();
            // vm.getFormValues();
            vm.isSaving = true;
            if (vm.prametreExport.id !== null) {
                console.log("Je suis dans la modiofication");
                console.log(vm.prametreExport);
                PrametreExport.update(vm.prametreExport, onSaveSuccess, onSaveError);
            } else {
                console.log("Je suis dans la creation");
                console.log(vm.prametreExport);
                PrametreExport.save(vm.prametreExport, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:prametreExportUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        // if (vm.prametreExport.typeListe=="LISTE_STATIQUE"){
        //     console.log("yes");
        //     vm.inputs = [];
        //     vm.cols = vm.prametreExport.valeur.split(';');
            
        //     angular.forEach(vm.cols, function(c) {
        //         console.log("------------------------------{}", c);
        //         if (c!=" ") {
        //             var elts = c.split(':');
        //             var newInput = {
        //                 idCol: 'inputCol'+ (vm.prametreExport.id++),
        //                 valueCol: elts[0],
        //                 idVal: 'inputVal'+ (vm.prametreExport.id++),
        //                 valueVal: elts[1]
        //             };
        //             console.log(newInput);
        //             vm.inputs.push(newInput);
        //         }
        //     })

        // }else{
        //     vm.inputs = [{
        //         idCol: 'inputCol1',
        //         valueCol: '',
        //         idVal: 'inputVal1',
        //         valueVal: ''
        //       }
        //     ];
        // }
                  
        // vm.addInput = function() {
        //     var newInput = {
        //         idCol: 'inputCol'+ (vm.inputs.length + 1),
        //         valueCol: '',
        //         idVal: 'inputVal'+ (vm.inputs.length + 1),
        //         valueVal: ''
        //     };
        //     vm.inputs.push(newInput);
            
        // };

        
        // vm.getFormValues = function() {
        //     vm.prametreExport.valeur = '';
        //     angular.forEach(vm.inputs, function(input) {
        //         var formValues = {};
        //         formValues[input.idCol] = input.valueCol;
        //         formValues[input.idVal] = input.valueVal;
        //         if (input.valueCol!='' && input.valueVal!='') {
        //             vm.couple = input.valueCol+":"+input.valueVal+"; ";
        //             vm.prametreExport.valeur = vm.prametreExport.valeur+vm.couple;
        //         }
                
        //     });
        // };
        console.log("------------------{}", vm.IsList);

        vm.clearIfNotList = function (){
            if (!vm.IsList){
                vm.prametreExport.tableRef = null;
                vm.prametreExport.valeur = null;
                vm.prametreExport.nomChamps = null;
            }
            vm.prametreExport.tableRef = null;
            vm.prametreExport.valeur = null;
            vm.prametreExport.nomChamps = null;
        };

        vm.deleteCol = function (input) {
            console.log("===============================================");
            console.log(input);
            var index = vm.inputs.indexOf(input);
            vm.inputs.splice(index, 1);
            if (vm.prametreExport.valeur){
                var val = vm.prametreExport.valeur;
                console.log(val);
                var rm = input.valueCol+":"+input.valueVal+";";
                console.log(rm);
                val.replace(rm, '');
                console.log("------------------------------------------------");
                console.log(val);
            }
        }

    }
})();
