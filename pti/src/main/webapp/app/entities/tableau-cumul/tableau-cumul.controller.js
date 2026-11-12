(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauCumulController', TableauCumulController);

    TableauCumulController.$inject = ['TypeService', 'RequestUtils', 'InitCrud', '$scope', '$state', 'TableauCumul'];

    function TableauCumulController(TypeService, RequestUtils, InitCrud, $scope, $state, TableauCumul) {

        var vm = this;
        vm.title = "Tableau de cumuls";
        vm.itemsFilter = [
        ];

        //Construction des champs du formulaire pour le filtre
        RequestUtils.GetOrganisationByType.get({ type: "PAYS" }, function (datas) {
            vm.pays = datas;
            vm.itemsFilter.push(
                [
                    {
                        name: 'uoEmId',
                        model: null,
                        title: "Choisir l'organisation em",
                        datas: vm.pays,
                        type: "select",
                        options: "data.id as data.libelleUniteOrganisation for data in col.datas"
                    }, {
                        name: 'uoAcqId',
                        model: null,
                        title: "Choisir l'organisation acq",
                        datas: vm.pays,
                        type: "select",
                        options: "data.id as data.libelleUniteOrganisation for data in col.datas"
                    }
                ]
            );
            TypeService.query({}, function (datas) {
                vm.itemsFilter.push(
                    [
                        {
                            name: 'typeServiceId',
                            model: null,
                            title: 'Choisir le type de service',
                            datas: datas,
                            type: "select",
                            options: "data.id as data.libelle for data in col.datas"
                        }
                    ]
                );
            });
        });



        //Pour la construction du tableau  
        vm.columns = [{
            name: 'typeServiceLibelle',
            title: 'Type de service'
        }, {
            name: 'typeRegle',
            title: 'TypeRegle'
        }, {
            name: 'temps',
            title: 'Critère'
        }, {
            name: 'organisationEmLibelleUniteOrganisation',
            title: 'Organisation Em'
        }, {
            name: 'organisationAcqLibelleUniteOrganisation',
            title: 'Organisation Acq'
        }, {
            name: 'montant',
            title: 'Montant'
        }, {
            name: 'nombre',
            title: 'Nombre'
        }, {
            name: 'activated',
            title: 'Statut',
            type:'Boolean'
        }];

        vm.actions = {
            view: false,
            edit: true,
            delete: true,
            add: true
        };

        var datas = {
            vm: vm,
            bodyClass: "col-sm-12",
            entityDatas: {
                poidsNum: null,
                codeRegle: null,
                temps: null,
                nombre: null,
                montant: null,
                action: null,
                dateDebut: null,
                dateFin: null,
                id: null
            },
            entity: TableauCumul,
            addTemplateUrl: "tableau-cumul/tableau-cumul-dialog.html",
            deleteTemplateUrl: "tableau-cumul/tableau-cumul-delete-dialog.html",
            controller: "TableauCumul",
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
    }
})();
