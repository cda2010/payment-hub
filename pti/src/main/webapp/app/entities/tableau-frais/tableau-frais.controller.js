(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauFraisController', TableauFraisController);

    TableauFraisController.$inject = ['TypeService','RequestUtils','InitCrud', 'TableService', '$scope', '$state', 'TableauFrais', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TableauFraisController(TypeService,RequestUtils,InitCrud, TableService, $scope, $state, TableauFrais, ParseLinks, AlertService, paginationConstants, pagingParams) {


        var vm = this;
        vm.title = "Liste des frais";
        vm.state = 'frais-ligneFrais({ tableauFrais:item})';//details
        vm.stateView = "frais-ligneFrais";//details
        vm.statePrincipal="tableau-frais";

        vm.itemsFilter=[
        ];

        //Construction des champs du formulaire pour le filtre
        RequestUtils.GetOrganisationByType.get({ type: "PAYS" }, function(datas) {
            vm.pays = datas;
            vm.itemsFilter.push(
                [
                    {
                        name: 'uoEmId',
                        model:null,
                        title: "Choisir l'organisation em",
                        datas:vm.pays,
                        type:"select",
                        options:"data.id as data.libelleUniteOrganisation for data in col.datas"
                    },{
                        name: 'uoAcqId',
                        model:null,
                        title: "Choisir l'organisation acq",
                        datas:vm.pays,
                        type:"select",
                        options:"data.id as data.libelleUniteOrganisation for data in col.datas"
                    }
                ]
            );
            TypeService.queryAll({},function(datas){
                vm.itemsFilter.push(
                    [
                        {
                            name: 'typeServiceId',
                            model:null,
                            title: 'Choisir le type de service',
                            datas:datas,
                            type:"select",
                            options:"data.id as data.libelle for data in col.datas"
                        }
                    ]
                );
            });
        });

        

        //Pour la construction du tableau  
        vm.columns = [ {
            name: 'typeServiceLibelle',
            title: 'Type de service'
        },{
            name: 'libelle',
            title: 'Libellé'
        }
        , {
            name: 'canalLibelleCanal',
            title: 'Canal'
        }, {
            name: 'organisationEmLibelleUniteOrganisation',
            title: 'Organisation Em'
        }, {
            name: 'organisationAcqLibelleUniteOrganisation',
            title: 'Organisation Acq'
        },
        , {
            name: 'typeRegle',
            title: 'Qui paye?'
        },
            {
            name: 'activated',
            title: 'Statut',
            type:'Boolean'
        }];

        vm.actions = {
            view: false,
            edit: true,
            delete: true,
            add:true
        };

        var datas = {
            vm: vm,
            bodyClass:"col-sm-12",
            entityDatas: {
                code: null,
                libelle: null,
                id: null
            },
            entity: TableauFrais,
            addTemplateUrl: "tableau-frais/tableau-frais-dialog.html",
            deleteTemplateUrl: "tableau-frais/tableau-frais-delete-dialog.html",
            controller: "TableauFrais",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope,
            detailsState: vm.state,
            stateView: vm.stateView,
            isMasterDetails: true,
            hasFilter:true,
            items:vm.itemsFilter,
            state:$state,
            statePrincipal:vm.statePrincipal
        };
        InitCrud.init(datas);
    }
})();