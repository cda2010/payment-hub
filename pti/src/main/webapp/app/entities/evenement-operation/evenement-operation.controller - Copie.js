(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EvenementOperationController', EvenementOperationController);

    EvenementOperationController.$inject = ['$scope', '$state','InitCrud', 'EvenementOperation', '$translate'];

    function EvenementOperationController($scope, $state,InitCrud, EvenementOperation, $translate) {

        var vm = this;
        vm.title = $translate.instant('ptiApp.operation.listeEvenementOp');
        vm.itemsFilter = [
        ];
        vm.values=[
            {
                id:1,
                nom:"Oui"
            },
            {
                id:0,
                nom:"Non"
            }
        ];
        //Construction des champs du formulaire pour le filtre
        vm.itemsFilter.push(
            [
                {
                    name: 'telephone',
                    model: vm.telephone,
                    title: "Saisir le numéro",
                    type: "input"
                },
                {
                    name: 'estLevee',
                    model:null,
                    title: "Levé ?",
                    datas:vm.values,
                    type:"select",
                    options:"data.id as data.nom for data in col.datas"
                }
            ]
        );
        vm.columns = [{
            name: 'createdDate',
            type:'DateTime',
            //title: $translate.instant('evenementOperation.createDate')
            title:'Date de création'
        },{
            name: 'montant',
            title:'Montant'
            //title: $translate.instant('ptiApp.evenementOperation.montant')
        },{
            name: 'description',
            title:'Description',
            //title: $translate.instant('ptiApp.evenementOperation.operation.description'),
            type:'string'
        },{
            name: 'typeService',
            title:'Type de service',
            //title: $translate.instant('ptiApp.evenementOperation.operation.typeService'),
            type:'string'
        },
        {
            name: 'estLevee',
            title:'estLevee',
            //title: $translate.instant('ptiApp.evenementOperation.estLevee'),
            type:'Boolean'
        }];

        vm.actions = {
            view: true,
            edit: false,
            delete: false
        };

        var datas = {
            vm: vm,
            entityDatas: {
                montant: null,
                estLevee: null,
                id: null
            },
            entity: EvenementOperation,
            addTemplateUrl: "evenement-operation/evenement-operation-dialog.html",
            deleteTemplateUrl: "evenement-operation/evenement-operation-delete-dialog.html",
            controller: "EvenementOperation",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope,
            hasFilter: true,
            items: vm.itemsFilter,
            state: $state,
            statePrincipal: vm.statePrincipal
        };
        InitCrud.init(datas);
        console.log(vm.itemsFilter);
        /* vm.evenementOperations = [];

        loadAll();

        function loadAll() {
            EvenementOperation.query(function(result) {
                vm.evenementOperations = result;
                vm.searchQuery = null;
            });
        } */
    }
})();
