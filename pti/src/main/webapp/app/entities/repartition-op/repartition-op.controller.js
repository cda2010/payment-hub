(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartitionOpController', RepartitionOpController);

        RepartitionOpController.$inject = ['InitCrud', '$scope', 'RepartitionOp'];

        function RepartitionOpController(InitCrud, $scope, RepartitionOp) {
            var vm = this;
            vm.title = "Liste des canaux";
            vm.columns = [{
                name: 'date',
                title: 'Date',
                type: 'DateTime'
            },{
                name: 'operationId',
                title: 'Operation'
            }, {
                name: 'acteur',
                title: 'Acteur'
            }, {
                name: 'montant',
                title: 'Montant'
            }];
            vm.actions = {
                view: false,
                edit: false,
                delete: false,
            };
    
            var datas = {
                vm: vm,
                entityDatas: {
                    codeRepartitionOp: null,
                    libelleRepartitionOp: null,
                    id: null
                },
                entity: RepartitionOp,
                addTemplateUrl: "",
                deleteTemplateUrl: "",
                controller: "RepartitionOp",
                entityParamsItem: {},
                entitySearchParamsItem: {},
                size: "md",
                scope: $scope
            };
            InitCrud.init(datas);
        }
    })();