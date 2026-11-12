(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LogOperationController', LogOperationController);

        LogOperationController.$inject = ['TableService', '$scope', 'LogOperation'];

        function LogOperationController(TableService, $scope, LogOperation) {
            var vm = this;
            vm.filtrer=false;
            vm.itemSearch = {
                "codeOperation": null,
                "login": null,
                "debut":null,
                "fin":null
            };
            vm.initFiltre = function () {
                vm.filtrer = !vm.filtrer;
                if (!vm.filtrer) {
                    vm.itemSearch = {
                        "codeOperation": null,
                        "login": null,
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
                        "codeOperation": vm.itemSearch.codeOperation,
                        "login": vm.itemSearch.login,
                        "debut": vm.itemSearch.debut,
                        "fin": vm.itemSearch.fin
                    };
                };

                var datas = {
                    vm: vm,
                    scope: $scope,
                    entityDatas: {
                        dateOperation: null,
                        description: null,
                        login: null,
                        result: null,
                        codeOperation: null,
                        id: null
                    },
                    entity: LogOperation,
                    entitySearch: LogOperation,
                    controller: "LogOperation",
                    entitySearchParams: entitySearchParams,
                    entityParams: entityParams,
                };
                TableService.init(datas);
            }
        }
    })();