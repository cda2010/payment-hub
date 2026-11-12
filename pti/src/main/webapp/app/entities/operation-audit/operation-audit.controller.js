(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationAuditController', OperationAuditController);

        OperationAuditController.$inject = ['TableService','$scope','$state', 'OperationAudit', 'RequestUtils', 'AlertService', 'paginationConstants', 'pagingParams'];

        function OperationAuditController(TableService,$scope,$state, OperationAudit, RequestUtils, AlertService, paginationConstants, pagingParams) {
    
            var vm = this;
            vm.statuts=[
                {
                    nom:"ENCOURS"
                },{
                    nom:"SUSPENDU"
                },{
                    nom:"PAYE"
                },{
                    nom:"ENATTENTE"
                },{
                    nom:"ENATTENTE_VALIDATION"
                }
            ];
            vm.filtrer=false;
            vm.itemSearch = {
                "statutOperation": null,
                "statutApres": null,
                "debut":null,
                "fin":null
            };
            vm.initFiltre = function () {
                vm.filtrer = !vm.filtrer;
                if (!vm.filtrer) {
                    vm.itemSearch = {
                        "statutOperation": null,
                        "statutApres": null,
                        "debut":null,
                        "fin":null
                    };
                    init();
                }
            }
            vm.filtre = function () {
                console.log("filtre des informations en cours");
                init();
            }
            init();
            function init() {
                var entitySearchParams = function() {
                    return {
                    };
                };
                var entityParams = function() {
                    return {
                        "statutOperation": vm.itemSearch.statutOperation,
                        "statutApres":     vm.itemSearch.statutApres,
                        "debut": vm.itemSearch.debut,
                        "fin": vm.itemSearch.fin
                    };
                };
                var datas={
                    vm:vm,
                    entityDatas:{
                        code: null,
                        libelle: null,
                        id: null
                    },
                    entity:OperationAudit,
                    entitySearch: OperationAudit,
                    controller:"OperationAudit",
                    entitySearchParams: entitySearchParams,
                    entityParams: entityParams,
                    scope:$scope,
                };
                TableService.init(datas);
            }
        }
    })();