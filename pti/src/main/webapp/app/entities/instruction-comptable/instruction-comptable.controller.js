(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InstructionComptableController', InstructionComptableController);

    InstructionComptableController.$inject = ['$scope','RequestUtils','$state', 'InstructionComptable','TableService'];

    function InstructionComptableController($scope,RequestUtils,$state, InstructionComptable,TableService) {

        var vm = this;

        vm.filtrer=false;
        vm.itemSearch= {
            "typeServiceId": null,
            "instruction": null,
            "etiquetteComptable": null,
            "montant": null,
        };
        vm.typeServices = RequestUtils.TypeServiceList.query();
        vm.etiquetteComptes = RequestUtils.EtiquetteCompteList.query();

        vm.initFiltre=function(){
            vm.filtrer=!vm.filtrer;
            if(!vm.filtrer){
                vm.itemSearch= {
                    "typeServiceId": null,
                    "instruction": null,
                    "etiquetteComptable": null,
                    "montant": null,
                };
                init();
            }
        }

        vm.filtre=function(){
            console.log("filtre des informations en cours");
            console.log(vm.itemSearch);
            init();
        }
        init();
        function init(){
            var addItemData = {
                templateUrl: 'app/entities/instruction-comptable/instruction-comptable-dialog.html',
                controller: 'InstructionComptableDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function() {
                        return {
                            numeroOrdre: null,
                            instruction: null,
                            etiquetteComptable: null,
                            typeServiceId : null,
                            montant: null,
                            id: null
                        };
                    },
                    isViewing: false
                }
            };

            /**
             *Modification d'un élément
             */
            var editItemData = {
                templateUrl: 'app/entities/instruction-comptable/instruction-comptable-dialog.html',
                controller: 'InstructionComptableDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: null, //valeur déterminé à l'appel de la boite
                    isViewing: false
                }
            };

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {
                templateUrl: 'app/entities/instruction-comptable/instruction-comptable-delete-dialog.html',
                controller: 'InstructionComptableDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/instruction-comptable/instruction-comptable-dialog.html',
                controller: 'InstructionComptableDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: null, //valeur déterminé à l'appel de la boite
                    isViewing: true
                }
            };

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function() {
                return {

                };
            };


            var entityParams = function() {
                return {
                    "typeServiceId":vm.itemSearch.typeServiceId,
                    "instruction":vm.itemSearch.instruction,
                    "etiquetteComptable": vm.itemSearch.etiquetteComptable,
                    "montant": vm.itemSearch.montant,
                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity: InstructionComptable,
                entitySearch: InstructionComptable,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }
    }
})();
