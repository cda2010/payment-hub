(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TransactionComptePtiController', TransactionComptePtiController);

    TransactionComptePtiController.$inject = ['InitCrud','$scope','$state', 'TransactionComptePti', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TransactionComptePtiController(InitCrud,$scope,$state, TransactionComptePti, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title="Liste des transactions";

        vm.columns = [{
            name: 'date',
            title: 'Date',
            type:"DateTime"
        },{
            name: 'type',
            title: 'Type'
        },{
            name: 'montant',
            title: 'Montant'
        },{
            name: 'soldeCourant',
            title: 'Solde courant'
        },{
            name: 'soldeApres',
            title: 'Solde apres'
        },{
            name: 'etat',
            title: 'Etat'
        }];
        
        vm.actions={
            view:false,
            edit:false,
            delete:false,
            add:false
        };

        var datas={
            vm:vm,
            entityDatas:{
                codeTransactionComptePti: null,
                libelleTransactionComptePti: null,
                id: null
            },
            entity:TransactionComptePti,
            addTemplateUrl:"transaction-compte-pti/transaction-compte-pti-dialog.html",
            deleteTemplateUrl:"transaction-compte-pti/transaction-compte-pti-delete-dialog.html",
            controller:"TransactionComptePti",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();